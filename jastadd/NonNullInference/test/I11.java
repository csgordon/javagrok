package test;

public class I11 {
  void test() {
    Object v1 = null;
    Object v3 = null;
    Object v2 = new Object();
    if(v1 != null) {
      v1 = v3;
      v2 = v1;
    }
  }

  public static void main(String[] args) {
    System.out.println("guarded possibly null variables may be non-null locally, guard is invalid if re-assigned");
  }
}
