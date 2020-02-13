public class Code {

  public static void main(String []argv) {
    Class c = bar(Foo.class);
  }


  public static Class bar(Class c) {
   return c;
}
}
