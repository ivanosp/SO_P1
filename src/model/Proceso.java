package model;

import java.util.Locale;

public class Proceso {
    private String etiqueta;
    private double burstTime;
    private double arrivalTime;
    private int queue;
    private int priority;
    private double remainingTime;
    private double completionTime;
    private double waitingTime;
    private double responseTime;
    private double turnaroundTime;
    private boolean firstExecution;

    public Proceso(String etiqueta, double burstTime, double arrivalTime, int queue, int priority) {
        this.etiqueta = etiqueta;
        this.burstTime = burstTime;
        this.arrivalTime = arrivalTime;
        this.queue = queue;
        this.priority = priority;
        this.remainingTime = burstTime;
        this.completionTime = 0;
        this.waitingTime = 0;
        this.responseTime = -1;
        this.turnaroundTime = 0;
        this.firstExecution = true;
    }

    // Getters y Setters
    public String getEtiqueta() { return etiqueta; }
    public double getBurstTime() { return burstTime; }
    public double getArrivalTime() { return arrivalTime; }
    public int getQueue() { return queue; }
    public int getPriority() { return priority; }
    public double getRemainingTime() { return remainingTime; }
    public void setRemainingTime(double remainingTime) { this.remainingTime = remainingTime; }
    public double getCompletionTime() { return completionTime; }
    public void setCompletionTime(double completionTime) { this.completionTime = completionTime; }
    public double getWaitingTime() { return waitingTime; }
    public void setWaitingTime(double waitingTime) { this.waitingTime = waitingTime; }
    public double getResponseTime() { return responseTime; }
    public void setResponseTime(double responseTime) { this.responseTime = responseTime; }
    public double getTurnaroundTime() { return turnaroundTime; }
    public void setTurnaroundTime(double turnaroundTime) { this.turnaroundTime = turnaroundTime; }
    public boolean isFirstExecution() { return firstExecution; }
    public void setFirstExecution(boolean firstExecution) { this.firstExecution = firstExecution; }

    /**
     * Formatea un número como ENTERO (sin decimales)
     */
    private String formatEntero(double number) {
        return String.valueOf((long) Math.round(number));
    }

    @Override
    public String toString() {
        return String.format("%s;%s;%s;%d;%d;%s;%s;%s;%s",
                etiqueta,
                formatEntero(burstTime),
                formatEntero(arrivalTime),
                queue,
                priority,
                formatEntero(waitingTime),
                formatEntero(completionTime),
                formatEntero(responseTime),
                formatEntero(turnaroundTime));
    }
}