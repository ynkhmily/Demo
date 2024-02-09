# JLox 
A lox interpreter written in java based on http://www.craftinginterpreters.com
Note: 运行需指定文件

示例 1 流程控制
````
for( var i = 1;i <= 10;i = i + 1){
    print i;
    if( i == 5)     continue;
    if( i == 9)     break;
}
var i = 1;
while(i < 10){
    print i;
    i = i + 1;
}
````
示例 2 函数
````
// 匿名函数
fun fun1(fn) {
  for (var i = 1; i <= 3; i = i + 1) {
    fn(i);
  }
}

fun1(fun (a) {
  print a;
});

// 闭包
fun fun2(){
    var a = 1;
    fun fun3(){
        a = a + 1;
        print a;
    }
    return fun3;
}
var b = fun2();
b();
````
示例 3 类和继承
````
class A {
  method() {
    print "A method";
  }
}

class B < A {
  method() {
    super.method();
    print "B method";
  }
}
var a = B();
a.method();
````