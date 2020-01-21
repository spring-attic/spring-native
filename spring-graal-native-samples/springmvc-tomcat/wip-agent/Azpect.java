public aspect Azpect {
  before(String name): call(* ClassLoader.loadClass(..)) && args(name,..) {
    System.out.println("{\"name\":\""+name+"\",\"allDeclaredConstructors\":true},");
  }
}
