# TODO

This is a random collection of known problems, missing functionality
and other shortcomings in the application.

* There are a ton of hardcoded things --- many of these are due to the
  lack of proper testing BE (i.e. need for mock / scaffolding data),
  or for lack of a settings user interface (i.e. need testable
  defaults).

  * Scaffolding refers to S3 resources that might go away

  * ETH/currency value should be fetched dynamically.

  * ...

* The code related to NFC, beacon, and payment processing is a mess
  and would benefit from refactoring and addition of unit tests.

* There are no proper unit tests.

* In many places, the code uses dubious shortcuts, such as accessing
  `LiveData` contents directly, or using `runBlocking` when should
  instead switch the task to another thread or application or service
  thread instead of the UI thread.

* Plenty of unimplemented functionality exists, just search for
  `TODO()` in the code --- a lot of this is error handling, but also
  other stuff.

  * Ledger and backend integration was started, but never completed.
