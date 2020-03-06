# Design Decisions

## Threading

Inspired by Vert.x I have decided to use reactive APIs. This works very well when injecting a service with `nonblockign=true` which cashes a call util service is available and then executes it. This requires a reactive API. It is in genenral very nice when threading. 

That said, there are also complications with a reactive API. When a reactive API is called it will call the provided handler with a result before the initial call returns. The called handler may in turn do another call to another reactive API with its callback and the callstack keeps growing, and the originnal call has still not returned. 

One solution is to have each service submit the result handler callback to a threadpool. That is however not problem free either, and if the called handler only does something quick ad easy then doing that in a separate thread is overkill. APS is using a lot of Groovy code that adds more complications in this case. Groovy has "closures" that kind of looks like Java "lambdas", but really isn't. A groovy closure can be coerced into a lambda interface, but is still something quite different. Letting a service implemeted in Groovy submit a closure callig a hanndler to a thread pool will execute the handler in the service context. The closure will then call another closure provided by the caller. I've had problems with the second closure not executing in the expected context in this case. 

I also don't like that a service should take descisions for code that is not its own and which it does not know anything about. I have decided to not force pushing callback handlers to a thread pool. I see it as the callers responnsibility to determine if it in the callback handler needs to push some code to a thread pool. In that case for Groovy code the submited closure will execute in the callers context. If the callback does something quick then there is no point to push to a thread pool. If what is done in a callback hadler keeps adding to the call stack, making it deeper and deeper then code should be pushed to a thread pool. Decisions have to be made case by case.

 