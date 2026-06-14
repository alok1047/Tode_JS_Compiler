# Hidden-Test Stress Suite Report — ThunderJS

This report details the execution results of the stress suite designed to validate execution correctness and find edge cases in the ThunderJS interpreter.

| Status | Test File | Expected | Actual | Details |
| --- | --- | --- | --- | --- |
| PASS ✅ | `tests/array_methods/edges.js` | `empty map: <br>empty filter: <br>empty reduce: 100<br>single map: 10<br>single filter: 5<br>single reduce: 15` | `empty map: <br>empty filter: <br>empty reduce: 100<br>single map: 10<br>single filter: 5<br>single reduce: 15` | |
| PASS ✅ | `tests/array_methods/highorder.js` | `3,6,9<br>1,3<br>20<br>20<br>1<br>true<br>true` | `3,6,9<br>1,3<br>20<br>20<br>1<br>true<br>true` | |
| PASS ✅ | `tests/arrays/edges.js` | `0<br>undefined<br>100<br>undefined` | `0<br>undefined<br>100<br>undefined` | |
| PASS ✅ | `tests/arrays/methods.js` | `2,3<br>1,8,9,4<br>1,8,9,4,5,6<br>true<br>1<br>3<br>3-2-1<br>2,4,10,30` | `2,3<br>1,8,9,4<br>1,8,9,4,5,6<br>true<br>1<br>3<br>3-2-1<br>2,4,10,30` | |
| PASS ✅ | `tests/arrays/mutations.js` | `1,2,3<br>3<br>1<br>2` | `1,2,3<br>3<br>1<br>2` | |
| PASS ✅ | `tests/callbacks/custom.js` | `before<br>executing callback with: hello<br>after` | `before<br>executing callback with: hello<br>after` | |
| PASS ✅ | `tests/callbacks/iterators.js` | `2<br>4<br>6<br>4,8,12<br>8,12` | `2<br>4<br>6<br>4,8,12<br>8,12` | |
| PASS ✅ | `tests/coercion/constructors.js` | `123<br>true<br>false<br>123<br>true` | `123<br>true<br>false<br>123<br>true` | |
| PASS ✅ | `tests/coercion/globals.js` | `object<br>undefined<br>number<br>number<br>true<br>true<br>false` | `object<br>undefined<br>number<br>number<br>true<br>true<br>false` | |
| PASS ✅ | `tests/coercion/operators.js` | `55<br>10<br>2<br>25<br>NaN` | `55<br>10<br>2<br>25<br>NaN` | |
| PASS ✅ | `tests/conditionals/ifelse.js` | `positive even<br>zero<br>negative` | `positive even<br>zero<br>negative` | |
| PASS ✅ | `tests/conditionals/switchcase.js` | `1<br>2<br>default<br>1<br>2` | `1<br>2<br>default<br>1<br>2` | |
| PASS ✅ | `tests/date/constructors.js` | `0<br>1000<br>2025<br>5<br>13` | `0<br>1000<br>2025<br>5<br>13` | |
| PASS ✅ | `tests/date/conversions.js` | `1970-01-01T00:00:00.000Z<br>Thu, 01 Jan 1970 00:00:00 GMT<br>1970-01-01T00:00:00.000Z` | `1970-01-01T00:00:00.000Z<br>Thu, 01 Jan 1970 00:00:00 GMT<br>1970-01-01T00:00:00.000Z` | |
| PASS ✅ | `tests/date/getters.js` | `2025<br>5<br>13<br>10<br>30<br>45<br>123<br>5` | `2025<br>5<br>13<br>10<br>30<br>45<br>123<br>5` | |
| PASS ✅ | `tests/date/setters.js` | `2030<br>8<br>20<br>15<br>45<br>10<br>500<br>10000` | `2030<br>8<br>20<br>15<br>45<br>10<br>500<br>10000` | |
| PASS ✅ | `tests/edge_cases/comments.js` | `` | `` | |
| PASS ✅ | `tests/edge_cases/empty.js` | `` | `` | |
| PASS ✅ | `tests/edge_cases/nesting.js` | `100` | `100` | |
| PASS ✅ | `tests/errors/ref_misspelled_builtin.js` | `ReferenceError` | `❌ ReferenceError: comsole is not defined<br><br>File: tests/errors/ref_misspelled_builtin.js<br>Line: 5<br>Column: 1<br><br>5 \| comsole.log("hello");<br>    ^^^^^^^<br><br>💡 Did you mean:<br>   console` | |
| PASS ✅ | `tests/errors/ref_misspelled_function.js` | `ReferenceError` | `❌ ReferenceError: calculatTotal is not defined<br><br>File: tests/errors/ref_misspelled_function.js<br>Line: 8<br>Column: 1<br><br>8 \| calculatTotal();<br>    ^^^^^^^^^^^^^<br><br>💡 Did you mean:<br>   calculateTotal` | |
| PASS ✅ | `tests/errors/ref_misspelled_var.js` | `ReferenceError` | `❌ ReferenceError: usernme is not defined<br><br>File: tests/errors/ref_misspelled_var.js<br>Line: 6<br>Column: 13<br><br>6 \| console.log(usernme);<br>                ^^^^^^^<br><br>💡 Did you mean:<br>   username` | |
| PASS ✅ | `tests/errors/ref_undefined_var.js` | `ReferenceError` | `❌ ReferenceError: nonExistentVar is not defined<br><br>File: tests/errors/ref_undefined_var.js<br>Line: 5<br>Column: 13<br><br>5 \| console.log(nonExistentVar);<br>                ^^^^^^^^^^^^^^` | |
| PASS ✅ | `tests/errors/stack_trace_nested.js` | `TypeError` | `❌ TypeError: Cannot read property 'foo' of undefined<br><br>File: tests/errors/stack_trace_nested.js<br>Line: 7<br>Column: 13<br><br>7 \|   let b = a.foo;<br>                ^^^<br><br><br>Call Stack:<br>  at third()   line 7<br>  at second()   line 11<br>  at first()   line 15` | |
| PASS ✅ | `tests/errors/syntax_missing_brace.js` | `SyntaxError` | `❌ SyntaxError: Expected '}' after block at end of input<br><br>File: tests/errors/syntax_missing_brace.js<br>Line: 7<br>Column: 1<br><br>7 \| <br>    ^` | |
| PASS ✅ | `tests/errors/syntax_missing_paren.js` | `SyntaxError` | `❌ SyntaxError: Expected ')' after if condition at '{'<br><br>File: tests/errors/syntax_missing_paren.js<br>Line: 5<br>Column: 10<br><br>5 \| if (true {<br>             ^` | |
| PASS ✅ | `tests/errors/type_misspelled_property.js` | `TypeError` | `❌ TypeError: Cannot read property 'nme'<br><br>File: tests/errors/type_misspelled_property.js<br>Line: 6<br>Column: 16<br><br>6 \| let nme = user.nme;<br>                   ^^^<br><br>💡 Did you mean:<br>   name` | |
| PASS ✅ | `tests/errors/type_not_a_function.js` | `TypeError` | `❌ TypeError: 42 is not a function<br><br>File: tests/errors/type_not_a_function.js<br>Line: 6<br>Column: 3<br><br>6 \| x();<br>      ^` | |
| PASS ✅ | `tests/errors/type_property_of_undefined.js` | `TypeError` | `❌ TypeError: Cannot read property 'name' of undefined<br><br>File: tests/errors/type_property_of_undefined.js<br>Line: 6<br>Column: 17<br><br>6 \| let name = user.name;<br>                    ^^^^` | |
| PASS ✅ | `tests/functions/closures.js` | `15<br>20` | `15<br>20` | |
| PASS ✅ | `tests/functions/definitions.js` | `1<br>2<br>3` | `1<br>2<br>3` | |
| PASS ✅ | `tests/functions/parameters.js` | `15<br>11<br>30` | `15<br>11<br>30` | |
| PASS ✅ | `tests/loops/for.js` | `0<br>1<br>2<br>0<br>1<br>2<br>4<br>0,0<br>0,1<br>1,0<br>1,1` | `0<br>1<br>2<br>0<br>1<br>2<br>4<br>0,0<br>0,1<br>1,0<br>1,1` | |
| PASS ✅ | `tests/loops/while_dowhile.js` | `0<br>1<br>0<br>1` | `0<br>1<br>0<br>1` | |
| PASS ✅ | `tests/math/random.js` | `true` | `true` | |
| PASS ✅ | `tests/math/static.js` | `3<br>4<br>4<br>5<br>8<br>4<br>10<br>2` | `3<br>4<br>4<br>5<br>8<br>4<br>10<br>2` | |
| PASS ✅ | `tests/objects/properties.js` | `1<br>2<br>42<br>10<br>20<br>a,b<br>1,2<br>99` | `1<br>2<br>42<br>10<br>20<br>a,b<br>1,2<br>99` | |
| PASS ✅ | `tests/operators/arithmetic.js` | `8<br>2<br>15<br>1.6666666666666667<br>2<br>243` | `8<br>2<br>15<br>1.6666666666666667<br>2<br>243` | |
| PASS ✅ | `tests/operators/assignment.js` | `15<br>10<br>50<br>25` | `15<br>10<br>50<br>25` | |
| PASS ✅ | `tests/operators/coercion_edge.js` | `true<br>false<br>false<br>false<br>false<br>true` | `true<br>false<br>false<br>false<br>false<br>true` | |
| PASS ✅ | `tests/operators/comparisons.js` | `true<br>false<br>true<br>false<br>true<br>false<br>true<br>false` | `true<br>false<br>true<br>false<br>true<br>false<br>true<br>false` | |
| PASS ✅ | `tests/operators/logical.js` | `true<br>false<br>true<br>false<br>false<br>true` | `true<br>false<br>true<br>false<br>false<br>true` | |
| PASS ✅ | `tests/operators/updates.js` | `5<br>6<br>7<br>7<br>6<br>5<br>11<br>12` | `5<br>6<br>7<br>7<br>6<br>5<br>11<br>12` | |
| PASS ✅ | `tests/primitives/typeof.js` | `number<br>string<br>boolean<br>object<br>undefined` | `number<br>string<br>boolean<br>object<br>undefined` | |
| PASS ✅ | `tests/recursion/deep.js` | `500` | `500` | |
| PASS ✅ | `tests/recursion/factorial.js` | `120` | `120` | |
| PASS ✅ | `tests/recursion/fibonacci.js` | `13` | `13` | |
| PASS ✅ | `tests/spread_rest/arrays.js` | `1,2,3,4,5` | `1,2,3,4,5` | |
| PASS ✅ | `tests/spread_rest/objects.js` | `1<br>2<br>3` | `1<br>2<br>3` | |
| PASS ✅ | `tests/spread_rest/parameters.js` | `3<br>6<br>6` | `3<br>6<br>6` | |
| PASS ✅ | `tests/strings/edges.js` | `0<br>1<br>true<br>true<br>1000` | `0<br>1<br>true<br>true<br>1000` | |
| PASS ✅ | `tests/strings/methods.js` | `5<br>HELLO<br>hello<br>hello<br>hello <br> hello<br>a\|b\|c<br>hi hello<br>hi hi<br>script<br>script<br>4<br>true<br>true<br>true` | `5<br>HELLO<br>hello<br>hello<br>hello <br> hello<br>a\|b\|c<br>hi hello<br>hi hi<br>script<br>script<br>4<br>true<br>true<br>true` | |
| PASS ✅ | `tests/variables/basic.js` | `15` | `15` | |
| PASS ✅ | `tests/variables/const_assign.js` | `TypeError` | `❌ TypeError: Assignment to constant variable 'PI'<br><br>File: tests/variables/const_assign.js<br>Line: 6<br>Column: 1<br><br>6 \| PI = 4;<br>    ^^` | |
| PASS ✅ | `tests/variables/scoping.js` | `2<br>1` | `2<br>1` | |

## Final Summary
* **Total Tests:** 55
* **Passed:** 55
* **Failed:** 0
* **Success Rate:** 100.00%
