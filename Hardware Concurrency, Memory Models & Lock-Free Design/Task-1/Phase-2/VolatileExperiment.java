class VolatileExperiment {

    static int data = 0;
    static volatile boolean ready = false;

    public static void main(String[] args) throws Exception {

        Thread writer = new Thread(() -> {
            data = 42;
            ready = true;
        });

        Thread reader = new Thread(() -> {
            while (!ready) {
                Thread.onSpinWait();
            }

            System.out.println("Data = " + data);
        });

        writer.start();
        reader.start();

        writer.join();
        reader.join();
    }
}