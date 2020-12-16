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

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.navigation.fragment.findNavController
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import eu.sofie_iot.smaug.mobile.BuildConfig
import eu.sofie_iot.smaug.mobile.R
import eu.sofie_iot.smaug.mobile.ui.SmaugFragment
import io.noties.markwon.Markwon
import kotlinx.android.synthetic.main.fragment_about.*


class AboutFragment : SmaugFragment(R.layout.fragment_about) {
    private val TAG = "AboutFragment"
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d(TAG, "about text: ${getString(R.string.about_text)}")

        Markwon.create(requireContext()).setMarkdown(about_text, getString(R.string.about_text))

        third_party_licenses.setOnClickListener {
            findNavController().navigate(AboutFragmentDirections.actionLicenses())
            // startActivity(Intent(requireContext(), OssLicensesMenuActivity::class.java))
        }

        about_version.text = BuildConfig.VERSION_NAME
    }
}
