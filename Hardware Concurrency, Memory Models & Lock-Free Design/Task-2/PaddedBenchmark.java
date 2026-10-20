public class PaddedBenchmark {

    static class Counter {
        volatile long value = 0;

        long p1, p2, p3, p4, p5, p6, p7;
    }

    public static void main(String[] args) throws Exception {

        Counter c1 = new Counter();
        Counter c2 = new Counter();

        long start = System.nanoTime();

        Thread t1 = new Thread(() -> {
            for (int i = 0; i < 100000000; i++) {
                c1.value++;
            }
        });

        Thread t2 = new Thread(() -> {
            for (int i = 0; i < 100000000; i++) {
                c2.value++;
            }
        });

        t1.start();
        t2.start();

        t1.join();
        t2.join();

        long end = System.nanoTime();

        double time = (end - start) / 1000000.0;

        System.out.println("Counter 1: " + c1.value);
        System.out.println("Counter 2: " + c2.value);
        System.out.println("Time: " + time + " ms");
    }
}