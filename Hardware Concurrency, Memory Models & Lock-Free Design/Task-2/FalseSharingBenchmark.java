public class FalseSharingBenchmark {

    static class Counter {
        volatile long count1 = 0;
        volatile long count2 = 0;
    }

    public static void main(String[] args) throws Exception {

        Counter c = new Counter();

        long start = System.nanoTime();

        Thread t1 = new Thread(() -> {
            for (int i = 0; i < 100000000; i++) {
                c.count1++;
            }
        });

        Thread t2 = new Thread(() -> {
            for (int i = 0; i < 100000000; i++) {
                c.count2++;
            }
        });

        t1.start();
        t2.start();

        t1.join();
        t2.join();

        long end = System.nanoTime();

        double time = (end - start) / 1000000.0;

        System.out.println("Count 1: " + c.count1);
        System.out.println("Count 2: " + c.count2);
        System.out.println("Time: " + time + " ms");
    }
}