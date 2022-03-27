# owlery
- I'm going through the book [Crafting Interpreters](https://craftinginterpreters.com) and implementing my first programming language as I learn.
- This is related to the first part of the book, where the AST gets executed directly.

### Variables
```
a: 5
b: 2
c: a + b
name: "Alp"
```

### Control structures
```
if a % 2 = 0 {
    hoot "a is even"
} else {
    hoot "a is odd"
}
```

```
-- loop from 0 to 5 (exclusive)
loop 0 to 5 {
    -- do something
}

loop i: 0 to 5 {
    hoot i
}

-- same thing but to-part inclusive
loop i: 0 to incl 5 {
    hoot i
}

-- loop while the condition evaluates to true
loop myCondition {
    -- do something
}

-- COMING SOON: iteration loop
loop student in students {
    -- do something with the student
}
```