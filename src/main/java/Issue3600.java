import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;

public final class Issue3600 {
  public static void main(String[] args) {
    runSomeThreadsToIncreaseLoad();
    tryToReproduce();
  }

  private static void runSomeThreadsToIncreaseLoad() {
    for (int i = 0; i < 64; ++i) {
      new Thread(() -> {
        while (true) {}
      }).start();
    }
  }

  private static void tryToReproduce() {
    boolean[] failed = {false};
    Vertx vertx = Vertx.vertx();
    vertx.deployVerticle(new AbstractVerticle() {
      @Override
      public void start() {
        loop();
      }

      private void loop() {
        vertx.setTimer(1, ignore -> loop());
        vertx.deployVerticle(new AbstractVerticle() {}, new DeploymentOptions().setWorker(true), result -> {
          if (Vertx.currentContext() == context) {
            System.out.println("Everything fine");
          }
          else {
            System.out.println("Bug!!");
            failed[0] = true;
          }
        });
      }
    }, ignore1 -> vertx.close(ignore2 -> {
      if (failed[0]) {
        System.exit(0);
      }
      else {
        System.out.println("---");
        tryToReproduce();
      }
    }));
  }
}
