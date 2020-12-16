/*
 * Copyright 2020 Ericsson AB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package eu.sofie_iot.smaug.mobile.ui.views

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.view.isGone
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import eu.sofie_iot.smaug.mobile.R
import eu.sofie_iot.smaug.mobile.Util
import eu.sofie_iot.smaug.mobile.model.Bid
import eu.sofie_iot.smaug.mobile.model.LockerActivity
import eu.sofie_iot.smaug.mobile.model.Rent
import eu.sofie_iot.smaug.mobile.ui.HistoryLine
import eu.sofie_iot.smaug.mobile.ui.HistoryLine.Companion.asHistoryLine
import eu.sofie_iot.smaug.mobile.ui.LockerDetailsFragment
import kotlinx.android.synthetic.main.fragment_locker_details.*
import kotlinx.android.synthetic.main.fragment_locker_details.view.*
import kotlinx.android.synthetic.main.history_line.view.*
import kotlinx.android.synthetic.main.history_view.view.*


class HistoryLiveData(val activities: LiveData<List<LockerActivity>>,
                      val bids: LiveData<List<Bid>>,
                      val rents: LiveData<List<Rent>>
): MediatorLiveData<List<HistoryLine>>() {
    init {
        val update = object : Observer<Any> {
            override fun onChanged(t: Any?) {
                value =
                    (activities.value ?: emptyList()).map { it.asHistoryLine() } +
                            (rents.value ?: emptyList()).map { it.asHistoryLine() } +
                            (bids.value ?: emptyList()).map { it.asHistoryLine() }
            }
        }

        addSource(activities, update)
        addSource(bids, update)
        addSource(rents, update)
    }
}

open class HistoryView(context: Context, attrs: AttributeSet?) : ConstraintLayout(context, attrs) {
    companion object {
        val openHistoryLines = HashMap<Long, Boolean>()
        val TAG = "HistoryView"
    }

    private var adapter: HistoryLineAdapter = HistoryLineAdapter(context, emptyList())

    init {
        View.inflate(context, R.layout.history_view, this)

        history_view.layoutManager = LinearLayoutManager(context)
        history_view.adapter = adapter

        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.RentedUntilView,
            0, 0
        ).apply {
            try {
                collapsed = getBoolean(R.styleable.HistoryView_collapsed, false)
            } finally {
                recycle()
            }
        }

        history_label.setOnClickListener {
           // collapsed = !collapsed
            labelOnClickListener?.invoke()
        }
    }

    var labelOnClickListener: (() -> Unit)? = null

    var collapsed: Boolean
        set(value) {
            field = value
            history_view.isGone = collapsed
            history_label.setCompoundDrawablesWithIntrinsicBounds(
                if (collapsed) R.drawable.ic_baseline_expand_more_24
                else R.drawable.ic_baseline_expand_less_24,
                0, 0, 0)
        }


    var lines: List<HistoryLine> = emptyList()
        set(value) {
            field = value
            adapter.lines = value.sortedByDescending { it.time }
            adapter.notifyDataSetChanged()
            history_label.text = resources.getQuantityString(R.plurals.history_many,
                lines.size, lines.size)
        }

    private class HistoryLineHolder(val view: View) : RecyclerView.ViewHolder(view)

    private class HistoryLineAdapter(val context: Context, var lines: List<HistoryLine>)
        : RecyclerView.Adapter<HistoryLineHolder>() {
        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): HistoryLineHolder {
            val view =
                LayoutInflater.from(context).inflate(R.layout.history_line, parent, false)
            Log.d(TAG, "onCreateViewHolder: parent=$parent viewType=$viewType: view=$view")
            return HistoryLineHolder(view)
        }

        //val formatter = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)
        override fun onBindViewHolder(holder: HistoryLineHolder, position: Int) {
            Log.d(TAG, "onBindViewHolder: holder=$holder position=$position")
            lines[position].let { line ->
                holder.view.apply {
                    history_timestamp.text = Util.timeRelativeShort(context, line.time)
                    history_title.text = line.title
                    history_description.text = line.description
                    // default is gone, it'd be nice to remember the previous value
                    history_description.isGone =
                        line.description.isBlank() || !openHistoryLines.getOrDefault(line.id, false)

                    if (line.description.isNotBlank())
                        setOnClickListener {
                            history_description.isGone = !history_description.isGone
                            openHistoryLines.put(line.id, !history_description.isGone)
                        }
                }
            }
        }

        override fun getItemCount(): Int {
            Log.d(TAG, "getItemCount: ${lines.size}")
            return lines.size
        }
    }
}