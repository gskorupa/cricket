# Event handling

## In handling method we can

* process the event
* suspend the event by setting new timepoint and run Kernel.dispatch()
* change the event type (and optionally other parameters) and run Kernel.dispatch()

**do not run Kernel.dispatch() without modifying the event -> endless loop!**