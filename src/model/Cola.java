package model;

import java.util.LinkedList;
import java.util.Queue;

public class Cola {
    private int id;
    private String politica;
    private int quantum;
    private Queue<Proceso> procesos;

    public Cola(int id, String politica, int quantum) {
        this.id = id;
        this.politica = politica;
        this.quantum = quantum;
        this.procesos = new LinkedList<>();
    }

    public int getId() { return id; }
    public String getPolitica() { return politica; }
    public int getQuantum() { return quantum; }
    public Queue<Proceso> getProcesos() { return procesos; }

    public void agregarProceso(Proceso p) {
        procesos.add(p);
    }

    public boolean isEmpty() {
        return procesos.isEmpty();
    }

    public Proceso obtenerSiguiente() {
        if (isEmpty()) return null;

        switch (politica) {
            case "RR":
                return procesos.poll(); // FIFO
            case "SJF":
                return obtenerSJF();
            case "Priority":
                return obtenerPrioridad();
            default:
                return procesos.poll();
        }
    }

    private Proceso obtenerSJF() {
        Proceso seleccionado = null;
        double menorBurst = Double.MAX_VALUE;

        for (Proceso p : procesos) {
            if (p.getRemainingTime() < menorBurst) {
                menorBurst = p.getRemainingTime();
                seleccionado = p;
            }
        }
        if (seleccionado != null) {
            procesos.remove(seleccionado);
        }
        return seleccionado;
    }

    private Proceso obtenerPrioridad() {
        Proceso seleccionado = null;
        int mayorPrioridad = -1;

        for (Proceso p : procesos) {
            if (p.getPriority() > mayorPrioridad) {
                mayorPrioridad = p.getPriority();
                seleccionado = p;
            }
        }
        if (seleccionado != null) {
            procesos.remove(seleccionado);
        }
        return seleccionado;
    }

    public boolean tieneProcesos() {
        return !procesos.isEmpty();
    }

    public int tamaño() {
        return procesos.size();
    }
}