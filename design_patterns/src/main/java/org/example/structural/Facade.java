package org.example.structural;

/**
 * Facade — provides a simplified interface to a complex subsystem.
 * Example: home theater system (projector, sound, lights) behind one facade.
 */
public class Facade {

    static class Projector {
        void on()  { System.out.println("Projector on");  }
        void off() { System.out.println("Projector off"); }
    }

    static class SoundSystem {
        void on()          { System.out.println("Sound on");             }
        void setVolume(int v) { System.out.println("Volume: " + v);      }
        void off()         { System.out.println("Sound off");            }
    }

    static class Lights {
        void dim(int level) { System.out.println("Lights dimmed to " + level + "%"); }
        void on()           { System.out.println("Lights on");                        }
    }

    static class HomeTheaterFacade {
        private final Projector   projector   = new Projector();
        private final SoundSystem sound       = new SoundSystem();
        private final Lights      lights      = new Lights();

        public void watchMovie() {
            System.out.println("--- Starting movie ---");
            lights.dim(10);
            projector.on();
            sound.on();
            sound.setVolume(20);
        }

        public void endMovie() {
            System.out.println("--- Ending movie ---");
            sound.off();
            projector.off();
            lights.on();
        }
    }

    public static void main(String[] args) {
        HomeTheaterFacade theater = new HomeTheaterFacade();
        theater.watchMovie();
        theater.endMovie();
    }
}
