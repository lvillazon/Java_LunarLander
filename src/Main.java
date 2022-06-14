public class Main {

    public static void main(String[] args) {
        System.out.println("Hello Moon");
        Moon moon = new Moon();
        Lander apollo11 = new Lander(moon.startPosition());
        moon.addLander(apollo11);
    }
}
