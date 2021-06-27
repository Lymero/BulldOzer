# BuldOzer

BuldOzer is a programming language that uses the [Autumn] parsing library, the [Uranium] semantic analysis library and is interpreted in Java.
It is a mixture of objectless Java and a functional language.

The implementation still contains some quirks, I would not recommend anyone to use it.


[Autumn]: https://github.com/norswap/autumn
[Uranium]: https://github.com/norswap/uranium

# Features
## Literals
BulldOzer has the following literals: `integer` (Java long, can be negative), `bool` (Java boolean), `string` (Java String), `array` (Java ArrayList), `map` (Java Map), and the special literal `unknown`. Literals are final, which means that it is not possible to assign a value to them, e.g. `5 = 6` is invalid. A literal can be strongly typed, in which case it guarantees that the value matches the type.
```
integer x = 5
bool x = true
bool y = !true
string x = "5"
array<integer> x = [1, 2]
map<string, integer> x = {"a" : 5}
```
## Comments
A line comment starts with a hash sign (`#`).
```
# this a comment
# this is another comment
```
## Variable definition
A variable is declared alongside its expected type. A variable exists only within its scope, otherwise it is not accessible as a reference elsewhere. The expected type must match the type of the given value, unless the expected type is `var`, in which case the type is inferred. Variables must be initialized when declared.
```
string key = "1"
integer value = 5
array<integer> x = [value, value]       # x = [5, 5]
map<string, integer> y = {key : value}  # y = {"1":5}
```
## Arithmetic
A numerical arithmetic expression (`+`, `-`, `*`, `/`, `%`) must only have integer values as operands. The conventional operator precedence is applied. Dividing by 0 causes an exception to be thrown by the interpreter. The unary negation is also possible. The result of a numeric expression is an integer.
```
integer x = 1 + 2 * 3   # x = 7
integer y = -1 * 5 / 5  # y = -1
integer z = 5 / 2       # z = 2
```
## Conditionals
Numerical comparisons (`<`, `<=`, `>`, `>=`) must have integer values as operands, boolean logics (``, `&&`, `!`) must be performed with bool values as operands and equalities (`==`, `!=`) must have operands of the same type. Strings, arrays, and maps are compared by value and not by reference. The `if` statement takes a condition, if the condition is evaluated to true then its body is executed, otherwise the else statement (optional) is executed. It is possible to nest `if` statements.
```
integer x = 1
integer y = 5
if(x < y && !false) {
    print(x) 
} else { 
    print(y) 
} # 1 is printed
```
## Elif
If statements can have several optional elif statements.  
```
var x = 3 
if(x <= 3) { 
    print(1) 
} elif (x > 5) { 
    print(2)
} else { 
    print(3)
}
```
## While loop
The condition is similar to the `if` statement. The while continues to loop as long as the condition is evaluated as true. It is possible to nest `while` statements.
```
integer x = 1 
while(x < 3) {
    print(x) 
    x = x + 1
} # 1 2 are printed
```
## For-in loop
A for-in loop iterates through an array, tuple or map, and passes the current element to the `cursor`. The array (tuple or map) must be a valid array (tuple or map) or a valid array (tuple or map) reference. The cursor is only accessible  within the scope of the loop. The type of the cursor is inferred. When a map is given, the cursor receives a pair, which can be accessed as a tuple of 2 elements.
```
for (x : {"a": 1, "b": 2}) { 
    print(x[0]) print(x[1]) # prints each entry
} 
```
## Function
The type of the function is its return type, which must match the type of the returned expression. The function must have a return statement if its return type is not `void`. When calling the function, the number of arguments must match the number of parameters required in the declaration and their types must match.
```
func integer succ(integer n) { 
    ret n + 1 
} 
integer n = succ(5) # n = 6
```
## Default parameters
The default values of a function's parameters must match the given type. They influence the number of parameters required and they must be used with named parameters, unless the caller is only using default parameters.
```
func integer f(integer x = 5, integer z = 6) { 
    ret x + z 
} 
integer sum = f() # sum = 11
```
## Named parameters
Named parameters free the developer from the default order and allow default parameters to be used more easily. Semantic analysis ensures that only named parameters or no named parameters are used, and that there are no duplicate named parameters. It also checks that named parameters can be mapped to a parameter of the function. 
```
func integer f(integer x, integer y) { 
    ret x / y 
} 
integer div =  f(y: 2, x: 6) # div = 3
```
## Array
All elements of an array must match the declaration type unless the expected type is `var` (inference). The array type is inferred when the array is empty. To access an array, the key must be an integer. Elements can be accessed or added using the `[]` notation, if there is an element at the index then it is replaced, otherwise the element is added at the correct index. Indices without element contain `unknown`.
```
array<integer> x = [1, 1, 2]
integer y = x[0]    # y = 1
x[0] = 0            # x = [0, 1, 2]
x[4] = 4            # x = [0, 1, 2, unknown, 4]
```
## Tuple literal
A tuple must contain elements that match the type of the tuple. Unlike an array, a tuple has a fixed size, cannot be concatenated and is read-only.
```
tuple<integer> = (1, 2)
integer x = t[0] # x = 1
```
## Map
All pairs of types `<key, value>` in the map must have the same types and match the declaration type, unless the expected type is `var`. To access a map, the key must match the expected type. The map type is inferred when the map is empty. A map is composed of several entries. Elements can be added with the `[]` notation, if the key already exists then the value is replaced, otherwise the pair is added.
```
map<string, integer> m = {"1": 1, "2": 2}
integer x = m["1"]  # x = 1
m["3"] = 3          # m = {"1": 1, "2": 2, "3": 3}
m["3"] = x          # m = {"1": 1, "2": 2, "3": 1}
integer y = m["10"] # y = unknown
```
## Negative indexing
Arrays can be accessed with a negative index, e.g. the index `-1` corresponds to the last element of the array.
```
array<integer> a = [1, 2, 3, 4, 5] 
integer x = x[-2] # x = 4
```
## String program parameters
They are passed as implicit parameters `$0`, `$1` and so on. They have the type `string`. They must be accessed with an index of type `integer` (literals or reference). The number of parameters (type `integer`) is passed in the `$@` parameter. Parameters are passed to the constructor of the interpreter `new Interpreter(reactor, args)` (Java varargs). 
```
# with new Interpreter(reactor, "a", "b", "c")
integer x = 0 
print($x) # a
print($1) # b
print($@) # 3
```
## Printing to stdout
The function `print` does not return anything, and accepts any type as parameter. It is registered by default in the root scope so it can be resolved anywhere. It uses the Java `System.out.print` method.
```
print("a")          # prints a
print(1)            # prints 1
print(true)         # prints true
print([1, 2, 3])    # prints [1, 2, 3]
print({1: true})    # prints {1=true}
print(unknown)      # prints unknown
```
## Parsing strings to integers
The function `parseToInt` returns a string and accepts an integer. It is registered in the root scope so it can be resolved anywhere.
```
integer x = parseToInt("5") # x = 5
```

## Type inference
Literals, arrays, maps, tuples, functions can all be bound to a variable that uses the type `var`, the actual type will then be inferred from the value. Function can return `var` as well, the actual type will be inferred by looking at the returned value.
```
var x = 5               # infers integer
var a = [true, false]   # infers array<bool>
bool b = a[0]           # b = true
```
## Functions as values with closure \& inner functions
Functions can be assigned to variables using type inference. The type of the variable is the return type and the types of the function's parameters. A function can also return a function if its return type is `var`. Such function is stored alongside its environment, thus creating a closure that is used when the returned function is called.
```
func var outer(integer x) {
    func integer inner(integer y) { 
        ret x + y 
    }
    ret inner
}
var inner = outer(5)
print(inner(3)) # prints 8
```
## String, array and map concatenation
Using the `+` operator, it is possible to concatenate 2 strings, or arrays, or maps together. If an array (or map) is empty, then the resulting type is the type of the not-empty array (or map), otherwise their types must match.
```
var s = "Hello" + " " + "world."    # s = "Hello world."
var a = [1, 2] + [1] + [2]          # a = [1, 2, 1, 2]
var m = {1: true} +  {2: false}     # m = {1=true, 2=false}
```
## String interpolation
All strings are interpolated strings by default, which means a string literal might contain interpolation expressions. Such expressions are between the special `{}` characters. It is possible to have nested interpolations.
```
string item = "sock"
integer price = 5
string s = "The {item} is worth {price} dollars" 
# s = "The sock is worth 5 dollars"
```
