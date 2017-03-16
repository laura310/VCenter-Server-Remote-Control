
public class TestTrim {
	public static void main(String[] args) {
		//test trim
//		String a = "   today is a good day  ".trim().replaceAll(" +", " ");
//		System.out.println("$"+a+"$");
		
		//test to lower case
//		String b = "TOday Is a Good daY";
//		String c = b.toLowerCase();
//		System.out.println("$"+c+"$");
		
		//test string split
		String s = "this is a test";
		String[] tokens = s.split(" ");
		
		for(String t : tokens) {
			System.out.println(t);
		}
	}
	
}
