package main;

import entity.BoardingPassTrain;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.time.*;
import java.util.*;
import java.util.stream.IntStream;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Scanner;


public class UserInput {

    static Path filepath = Paths.get(System.getProperty("user.dir") + "/src/boarding_pass_ticket.txt");

    private static void write(Path filepath, String name, String origin, String destination, Date eta,
                              Date departure, String email, String phone, String gender, int age,
                              float ticketPrice) {
        try {
            Files.write(filepath, ("Your name: " + name + " Age: " + age + " Gender: " + gender + "\n").getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            Files.write(filepath, ("From: " + origin + " To: " + destination + "\n").getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            Files.write(filepath, ("Depature: " + departure + "Arrival: " + eta + "\n").getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            Files.write(filepath, ("Email: " + email + " Cellphone: " + phone + "\n").getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            Files.write(filepath, ("Ticket Price: " + ticketPrice).getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);

        } catch (Exception e){
            System.out.println("File does not exist");
            System.exit(-1);
        }
    }

    private static Scanner getInput = new Scanner(System.in);

    private static int getIntRange(int from, int to) {
        while (true) {
            try {
                int input = getInput.nextInt();
                if (input < from || input > to) {
                    System.out.print("Sorry, your input is outside the allowed range. Please try again: ");
                } else {
                    return input;
                }
            } catch (InputMismatchException e) {
                System.out.print("Sorry, I did not understand your input. Please try again: ");
            }
        }
    }

    private static int getInt() {
        return getIntRange(Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    public static void main(String[] args) throws ParseException {

        BoardingPassTrain pass1 = new BoardingPassTrain();
        String message = "Welcome to the World Fastest Train";
        System.out.print("+");
        IntStream.range(0, message.length()).forEach(i -> System.out.print("-"));
        System.out.printf("\n+%s+\n+", message);
        IntStream.range(0, message.length()).forEach(i -> System.out.printf("-%s", i != message.length() - 1 ? "" : "+\n"));

        //*** Name User Input ***
        System.out.print("Please enter your Name: ");
        String name = getInput.nextLine();
        pass1.setName(name);

        //*** Email User Input ***
        System.out.print("Please enter your Email: ");
        String email = getInput.nextLine();
        pass1.setEmail(email);

        //*** Phone User Input ***
        System.out.print("Please enter your Phone Number: ");
        String phoneNumber = getInput.nextLine();
        pass1.setPhone(phoneNumber);

        //*** Gender User Input ***
        System.out.print("Please enter your Gender: ");
        String gender = getInput.nextLine();
        pass1.setGender(gender);

        //*** Age User Input ***
        System.out.print("Please enter your Age: ");
        int age = getInt();
        pass1.setAge(age);

        List<String> destinations = DepartureTable.getDestinations();
        System.out.println("Please select a destination:");
        IntStream.range(0, destinations.size())
                .forEach(i -> System.out.printf("\t%d: %s\n", i + 1, destinations.get(i)));
        int choice = getIntRange(1, destinations.size());
        pass1.setDestination(destinations.get(choice - 1));

        List<Calendar> departureDates = DepartureTable.getDateByDestination(pass1.getDestination());
        System.out.println("Please select a departure date:");
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        for (int i = 0; i < departureDates.size(); i++) {
            System.out.printf("\t%d: %s\n", i + 1, formatter.format(departureDates.get(i).getTime()));
        }
        choice = getIntRange(1, departureDates.size());
        Calendar departure = departureDates.get(choice - 1);

        List<Calendar> departureTimes = DepartureTable.getTimeByDateAndDestination(departure, pass1.getDestination());
        System.out.println("Please select a departure time:");
        formatter = new SimpleDateFormat("HH:mm:ss");
        for (int i = 0; i < departureTimes.size(); i++) {
            System.out.printf("\t%d: %s\n", i + 1, formatter.format(departureTimes.get(i).getTime()));
        }
        pass1.setDeparture(departure.getTime());
    }

    public static Date calculateEta(Date departure, int distance, int speed){

        int time = distance/speed;
        Calendar cal = new GregorianCalendar();
        cal.setTime(departure);
        cal.add(Calendar.HOUR_OF_DAY,time);
        return cal.getTime();

    }

    public static float discount(float ticketPrice, int age, String gender) {
        if (age <= 12) {
            ticketPrice = ticketPrice * 0.5f;
        } else if (age >= 60) {
            ticketPrice = ticketPrice - (ticketPrice * 0.6f);
        } else if (gender.equals("Female")) {
            ticketPrice = ticketPrice - (ticketPrice * 0.25f);
        }
        return ticketPrice;
    }

    public static void saveTicket (String name, String origin, String destination, Date eta,
                                   Date departure, String email, String phone, String gender, int age,
                                   float ticketPrice) {
        SessionFactory factory = new Configuration().configure("hibernate.cfg.xml")
                .addAnnotatedClass(BoardingPassTrain.class)
                .buildSessionFactory();

        try {
            Session session = factory.getCurrentSession();
            session.beginTransaction();

            BoardingPassTrain myBoardingPassTrain = new BoardingPassTrain(name, origin, destination, eta,
                    departure, email, phone, gender, age, ticketPrice);
            session.save(myBoardingPassTrain);

            session.getTransaction().commit();

        } finally {
            factory.close();
        }
    }
}
