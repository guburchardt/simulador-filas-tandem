import java.util.PriorityQueue;
import java.util.Random;

public class SimuladorTandem {
    // configuração das filas
    private static final double ARRIVAL_MIN = 1.0;
    private static final double ARRIVAL_MAX = 4.0;
    private static final double SERVICE1_MIN = 3.0;
    private static final double SERVICE1_MAX = 4.0;
    private static final double SERVICE2_MIN = 2.0;
    private static final double SERVICE2_MAX = 3.0;
    
    private static Random rand = new Random();
    private static int totalCustomers = 0;
    private static int lostCustomers1 = 0;
    private static int lostCustomers2 = 0;
    private static double globalTime = 0;
    
    // Fila 1 (G/G/2/3)
    private static double[] queue1Time = new double[4]; // 0-3 clientes
    private static int queue1Current = 0;
    private static double queue1LastEventTime = 0;
    private static final int QUEUE1_SERVERS = 2;
    private static final int QUEUE1_CAPACITY = 3;
    
    // Fila 2 (G/G/1/5)
    private static double[] queue2Time = new double[6]; // 0-5 clientes
    private static int queue2Current = 0;
    private static double queue2LastEventTime = 0;
    private static final int QUEUE2_SERVERS = 1;
    private static final int QUEUE2_CAPACITY = 5;
    
    private static PriorityQueue<Event> scheduler = new PriorityQueue<>();
    
    public static void main(String[] args) {
        // Primeiro evento
        scheduler.add(new Event(1.5, EventType.ARRIVAL_1));
        
        // Simulação com 100.000 
        int randomNumbersUsed = 0;
        final int MAX_RANDOM_NUMBERS = 100000;
        
        while (randomNumbersUsed < MAX_RANDOM_NUMBERS && !scheduler.isEmpty()) {
            Event currentEvent = scheduler.poll();
            globalTime = currentEvent.time;
            
            // Atualiza estatísticas 
            updateQueueTimes();
            
            switch (currentEvent.type) {
                case ARRIVAL_1:
                    handleArrival1();
                    randomNumbersUsed++;
                    break;
                case DEPARTURE_1:
                    handleDeparture1();
                    randomNumbersUsed++;
                    break;
                case DEPARTURE_2:
                    handleDeparture2();
                    randomNumbersUsed++;
                    break;
            }
        }
        
        
        generateReport();
    }
    
    private static void updateQueueTimes() {
        // Atualiza tempo Fila 1
        double timeElapsed = globalTime - queue1LastEventTime;
        queue1Time[queue1Current] += timeElapsed;
        queue1LastEventTime = globalTime;
        
        // Atualiza tempo Fila 2
        timeElapsed = globalTime - queue2LastEventTime;
        queue2Time[queue2Current] += timeElapsed;
        queue2LastEventTime = globalTime;
    }
    
    private static void handleArrival1() {
       
        double nextArrivalTime = globalTime + uniform(ARRIVAL_MIN, ARRIVAL_MAX);
        scheduler.add(new Event(nextArrivalTime, EventType.ARRIVAL_1));
        
        
        if (queue1Current < QUEUE1_CAPACITY) {
            queue1Current++;
            totalCustomers++;
            
            
            if (queue1Current <= QUEUE1_SERVERS) {
                double departureTime = globalTime + uniform(SERVICE1_MIN, SERVICE1_MAX);
                scheduler.add(new Event(departureTime, EventType.DEPARTURE_1));
            }
        } else {
            lostCustomers1++;
        }
    }
    
    private static void handleDeparture1() {
        queue1Current--;
        
        
        if (queue1Current >= QUEUE1_SERVERS) {
            double departureTime = globalTime + uniform(SERVICE1_MIN, SERVICE1_MAX);
            scheduler.add(new Event(departureTime, EventType.DEPARTURE_1));
        }
        
        
        if (queue2Current < QUEUE2_CAPACITY) {
            queue2Current++;
            
            
            if (queue2Current <= QUEUE2_SERVERS) {
                double departureTime = globalTime + uniform(SERVICE2_MIN, SERVICE2_MAX);
                scheduler.add(new Event(departureTime, EventType.DEPARTURE_2));
            }
        } else {
            lostCustomers2++;
        }
    }
    
    private static void handleDeparture2() {
        queue2Current--;
        
        
        if (queue2Current >= QUEUE2_SERVERS) {
            double departureTime = globalTime + uniform(SERVICE2_MIN, SERVICE2_MAX);
            scheduler.add(new Event(departureTime, EventType.DEPARTURE_2));
        }
    }
    
    private static double uniform(double min, double max) {
        return min + (max - min) * rand.nextDouble();
    }
    
    private static void generateReport() {
        System.out.println("=== Relatório da Simulação ===");
        System.out.printf("Tempo global da simulação: %.2f minutos\n", globalTime);
        System.out.printf("Total de clientes atendidos: %d\n", totalCustomers);
        System.out.printf("Clientes perdidos na Fila 1: %d\n", lostCustomers1);
        System.out.printf("Clientes perdidos na Fila 2: %d\n", lostCustomers2);
        
        System.out.println("\n=== Estatísticas da Fila 1 (G/G/2/3) ===");
        System.out.println("Probabilidade de estados:");
        for (int i = 0; i <= QUEUE1_CAPACITY; i++) {
            double prob = queue1Time[i] / globalTime * 100;
            System.out.printf("%d clientes: %.2f%%\n", i, prob);
        }
        
        System.out.println("\n=== Estatísticas da Fila 2 (G/G/1/5) ===");
        System.out.println("Probabilidade de estados:");
        for (int i = 0; i <= QUEUE2_CAPACITY; i++) {
            double prob = queue2Time[i] / globalTime * 100;
            System.out.printf("%d clientes: %.2f%%\n", i, prob);
        }
    }
}

enum EventType {
    ARRIVAL_1, DEPARTURE_1, DEPARTURE_2
}

class Event implements Comparable<Event> {
    double time;
    EventType type;
    
    public Event(double time, EventType type) {
        this.time = time;
        this.type = type;
    }
    
    @Override
    public int compareTo(Event other) {
        return Double.compare(this.time, other.time);
    }
}