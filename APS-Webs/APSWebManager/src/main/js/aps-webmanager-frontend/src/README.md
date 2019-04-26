# Code standard

This code uses ES6 and JSX.

From what I've read ';' should be optional at end of statements. But I 
get warning markings if the first function/method in a function/method
does not end with a ';'. Other functions/methods can skip this without
complaints. 

Since much of the backend code uses Groovy which do not need the ';'
I'd like to do the same here, but that does not seem to work. I have 
determined that this is a bug in IDEA. The babel transpiler has no
problems with this. I'm striving as a minimum to have a green checkbox
in the upper right corner of the editor in IDEA, and false warnings
causes a problem. Thereby I stick with ';' for now. 
