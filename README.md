# EasySocket
A repository with sources for easily managing a socket in Java. Includes a socket watchdog for handling timeouts. Easily extendable.

The sources in the sources directory file can be included in your project. EasySocket can/should be extended. The basic implementation should suffice most functionality. If you choose to overwrite the start method make sure to call super.start(), otherwise the watchdog will not work.

These sources are provided under the UNLICENSE license. So feel free to do whatever you want with this code.
