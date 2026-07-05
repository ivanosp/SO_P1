package scheduler;

import model.Proceso;
import model.Cola;

import java.util.Comparator;
import java.util.stream.Collectors;

import java.io.*;
import java.util.*;

public class PlanificadorMLQ {
    private List<Cola> colas;
    private List<Proceso> procesosCompletados;
    private double tiempoActual;
    private List<Proceso> todosLosProcesos;
    private Map<Integer, Cola> colaPorId;
    private List<Proceso> procesosPendientes;
    private int indicePendientes;

    public PlanificadorMLQ() {
        this.colas = new ArrayList<>();
        this.procesosCompletados = new ArrayList<>();
        this.tiempoActual = 0;
        this.todosLosProcesos = new ArrayList<>();
        this.colaPorId = new HashMap<>();
        this.procesosPendientes = new ArrayList<>();
        this.indicePendientes = 0;
    }

    /**
     * Configura las colas según el esquema elegido
     * Esquema: RR(1), RR(3), SJF
     */
    public void configurarColas() {
        Cola cola1 = new Cola(1, "RR", 1);
        Cola cola2 = new Cola(2, "RR", 3);
        Cola cola3 = new Cola(3, "SJF", 0);

        colas.add(cola1);
        colas.add(cola2);
        colas.add(cola3);

        for (Cola c : colas) {
            colaPorId.put(c.getId(), c);
        }

        System.out.println("✅ Colas configuradas:");
        System.out.println("   Cola 1: RR(1) - Mayor prioridad");
        System.out.println("   Cola 2: RR(3) - Prioridad media");
        System.out.println("   Cola 3: SJF   - Menor prioridad");
    }

    /**
     * Carga los procesos desde un archivo de texto
     * SOLO los almacena en la lista de todos los procesos, NO los agrega a las colas todavía
     */
    public void cargarProcesos(String rutaArchivo) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(rutaArchivo));
        String linea;
        int contadorProcesos = 0;

        System.out.println("\n📂 Cargando archivo: " + rutaArchivo);

        while ((linea = br.readLine()) != null) {
            linea = linea.trim();
            if (linea.isEmpty() || linea.startsWith("#")) continue;

            String[] partes = linea.split(";");
            if (partes.length < 5) {
                System.out.println("⚠️ Línea ignorada (formato incorrecto): " + linea);
                continue;
            }

            try {
                String etiqueta = partes[0].trim();
                double burstTime = Double.parseDouble(partes[1].trim());
                double arrivalTime = Double.parseDouble(partes[2].trim());
                int queue = Integer.parseInt(partes[3].trim());
                int priority = Integer.parseInt(partes[4].trim());

                if (!colaPorId.containsKey(queue)) {
                    System.out.println("⚠️ Proceso " + etiqueta + " ignorado: Cola " + queue + " no existe");
                    continue;
                }

                Proceso proceso = new Proceso(etiqueta, burstTime, arrivalTime, queue, priority);
                todosLosProcesos.add(proceso);
                procesosPendientes.add(proceso);
                contadorProcesos++;

                System.out.println("   📥 Proceso " + etiqueta + " cargado (BT:" + burstTime +
                        ", AT:" + arrivalTime + ", Q:" + queue + ", Pr:" + priority + ")");

            } catch (NumberFormatException e) {
                System.out.println("⚠️ Error al parsear línea: " + linea);
            }
        }
        br.close();

        // ✅ ORDENAR: primero por Arrival Time, luego por Etiqueta (ascendente)
        procesosPendientes.sort(Comparator.comparingDouble(Proceso::getArrivalTime)
                .thenComparing(Proceso::getEtiqueta));

        todosLosProcesos.sort(Comparator.comparingDouble(Proceso::getArrivalTime)
                .thenComparing(Proceso::getEtiqueta));

        System.out.println("✅ Total de procesos cargados: " + contadorProcesos);

        // Mostrar el orden final (para verificar)
        String orden = procesosPendientes.stream()
                .map(p -> p.getEtiqueta() + "(AT:" + p.getArrivalTime() + ")")
                .collect(Collectors.joining(" → "));
        System.out.println("📋 Orden de ejecución (AT → Etiqueta): " + orden);
    }

    /**
     * Ejecuta el algoritmo de planificación MLQ
     */
    public void ejecutar() {
        System.out.println("\n⏳ Iniciando planificación...");
        System.out.println("═══════════════════════════════════════════════════");

        indicePendientes = 0;  // ← Reiniciar índice
        int procesosEjecutados = 0;

        while (!todosLosProcesosCompletados() || hayProcesosEnColas() || indicePendientes < procesosPendientes.size()) {

            // 1. Agregar procesos que han llegado a sus colas correspondientes
            while (indicePendientes < procesosPendientes.size() &&
                    procesosPendientes.get(indicePendientes).getArrivalTime() <= tiempoActual) {
                Proceso p = procesosPendientes.get(indicePendientes);
                indicePendientes++;
                Cola cola = colaPorId.get(p.getQueue());
                if (cola != null) {
                    cola.agregarProceso(p);
                    System.out.printf("📥 t=%.1f: Proceso %s llegó a Cola %d\n",
                            tiempoActual, p.getEtiqueta(), p.getQueue());
                }
            }

            // 2. Buscar la cola de mayor prioridad con procesos
            Cola colaActual = null;
            for (Cola c : colas) {
                if (c.tieneProcesos()) {
                    colaActual = c;
                    break;
                }
            }

            // 3. Si no hay procesos listos, avanzar el tiempo
            if (colaActual == null) {
                if (indicePendientes < procesosPendientes.size()) {
                    double siguienteLlegada = procesosPendientes.get(indicePendientes).getArrivalTime();
                    if (siguienteLlegada > tiempoActual) {
                        System.out.printf("⏸️  t=%.1f: No hay procesos listos. Avanzando a t=%.1f\n",
                                tiempoActual, siguienteLlegada);
                        tiempoActual = siguienteLlegada;
                    }
                } else {
                    break;
                }
                continue;
            }

            // 4. Obtener siguiente proceso de la cola
            Proceso proceso = colaActual.obtenerSiguiente();
            if (proceso == null) continue;

            // 5. Si es la primera vez que se ejecuta, registrar tiempo de respuesta
            if (proceso.isFirstExecution()) {
                proceso.setResponseTime(tiempoActual);
                proceso.setFirstExecution(false);
                System.out.printf("🟢 t=%.1f: %s (Cola %d) - Primera ejecución (RT=%.1f)\n",
                        tiempoActual, proceso.getEtiqueta(), proceso.getQueue(),
                        proceso.getResponseTime());
            }

            // 6. Calcular tiempo de ejecución según la política de la cola
            double tiempoEjecucion;
            if (colaActual.getPolitica().equals("RR")) {
                tiempoEjecucion = Math.min(colaActual.getQuantum(), proceso.getRemainingTime());
                System.out.printf("🔄 t=%.1f: %s ejecuta por %.1f (RR, quantum=%d, restante=%.1f)\n",
                        tiempoActual, proceso.getEtiqueta(), tiempoEjecucion,
                        colaActual.getQuantum(), proceso.getRemainingTime());
            } else {
                // SJF, STCF, Priority (no expropiativo)
                tiempoEjecucion = proceso.getRemainingTime();
                System.out.printf("⚡ t=%.1f: %s ejecuta por %.1f (%s, restante=%.1f)\n",
                        tiempoActual, proceso.getEtiqueta(), tiempoEjecucion,
                        colaActual.getPolitica(), proceso.getRemainingTime());
            }

            // 7. Ejecutar proceso (actualizar tiempo restante)
            proceso.setRemainingTime(proceso.getRemainingTime() - tiempoEjecucion);
            tiempoActual += tiempoEjecucion;
            procesosEjecutados++;

            // 8. Agregar nuevos procesos que llegaron durante la ejecución
            while (indicePendientes < procesosPendientes.size() &&
                    procesosPendientes.get(indicePendientes).getArrivalTime() <= tiempoActual) {
                Proceso p = procesosPendientes.get(indicePendientes);
                indicePendientes++;
                Cola cola = colaPorId.get(p.getQueue());
                if (cola != null) {
                    cola.agregarProceso(p);
                    System.out.printf("📥 t=%.1f: Proceso %s llegó a Cola %d\n",
                            tiempoActual, p.getEtiqueta(), p.getQueue());
                }
            }

            // 9. Verificar si el proceso terminó
            if (proceso.getRemainingTime() <= 0) {
                proceso.setCompletionTime(tiempoActual);
                proceso.setTurnaroundTime(proceso.getCompletionTime() - proceso.getArrivalTime());
                proceso.setWaitingTime(proceso.getTurnaroundTime() - proceso.getBurstTime());
                procesosCompletados.add(proceso);

                System.out.printf("✅ t=%.1f: %s COMPLETADO (WT=%.1f, CT=%.1f, RT=%.1f, TAT=%.1f)\n",
                        tiempoActual, proceso.getEtiqueta(),
                        proceso.getWaitingTime(), proceso.getCompletionTime(),
                        proceso.getResponseTime(), proceso.getTurnaroundTime());
                System.out.println("   ───────────────────────────────────────────────────────");
            } else {
                // 10. Si no terminó y es RR, volver a ponerlo en la cola
                if (colaActual.getPolitica().equals("RR")) {
                    colaActual.agregarProceso(proceso);
                    System.out.printf("🔄 t=%.1f: %s vuelve a Cola %d (restante=%.1f)\n",
                            tiempoActual, proceso.getEtiqueta(),
                            proceso.getQueue(), proceso.getRemainingTime());
                } else {
                    // Para SJF, STCF o Priority no expropiativo, si no terminó, se vuelve a agregar
                    colaActual.agregarProceso(proceso);
                    System.out.printf("⏳ t=%.1f: %s continúa en Cola %d (restante=%.1f)\n",
                            tiempoActual, proceso.getEtiqueta(),
                            proceso.getQueue(), proceso.getRemainingTime());
                }
            }
        }

        System.out.println("═══════════════════════════════════════════════════");
        System.out.println("✅ Planificación completada");
        System.out.println("📊 Total de procesos ejecutados: " + procesosEjecutados);
        System.out.println("⏱️  Tiempo total de simulación: " + tiempoActual);
    }

    /**
     * Verifica si todos los procesos han completado su ejecución
     */
    private boolean todosLosProcesosCompletados() {
        for (Proceso p : todosLosProcesos) {
            if (p.getRemainingTime() > 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * Verifica si hay procesos en alguna cola
     */
    private boolean hayProcesosEnColas() {
        for (Cola c : colas) {
            if (c.tieneProcesos()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Genera el archivo de salida con los resultados de la planificación
     */
    public void generarSalida(String rutaArchivo) throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter(rutaArchivo));

        // Escribir cabecera
        bw.write("# archivo: " + new File(rutaArchivo).getName() + "\n");
        bw.write("# etiqueta; BT; AT; Q; Pr; WT; CT; RT; TAT\n");

        // Ordenar por tiempo de finalización
        procesosCompletados.sort(Comparator.comparingDouble(Proceso::getCompletionTime));

        double sumaWT = 0, sumaCT = 0, sumaRT = 0, sumaTAT = 0;
        int n = procesosCompletados.size();

        // Escribir cada proceso
        for (Proceso p : procesosCompletados) {
            bw.write(p.toString() + "\n");
            sumaWT += p.getWaitingTime();
            sumaCT += p.getCompletionTime();
            sumaRT += p.getResponseTime();
            sumaTAT += p.getTurnaroundTime();
        }

        // Escribir promedios
        if (n > 0) {
            bw.write(String.format("# WT=%.1f; CT=%.1f; RT=%.1f; TAT=%.1f;\n",
                    sumaWT/n, sumaCT/n, sumaRT/n, sumaTAT/n));
        } else {
            bw.write("# No se procesaron procesos\n");
        }

        bw.close();

        System.out.println("\n📄 Archivo de salida generado: " + rutaArchivo);
        System.out.println("   Procesos completados: " + n);
    }

    /**
     * Método de diagnóstico para verificar el estado de las colas
     */
    public void diagnosticarColas() {
        System.out.println("\n🔍 DIAGNÓSTICO DE COLAS:");
        int totalProcesosEnColas = 0;

        for (Cola cola : colas) {
            System.out.println("   Cola " + cola.getId() + " (" + cola.getPolitica() +
                    "): " + cola.tamaño() + " procesos");
            for (Proceso p : cola.getProcesos()) {
                System.out.println("      - " + p.getEtiqueta() +
                        " (BT:" + p.getBurstTime() +
                        ", AT:" + p.getArrivalTime() +
                        ", Restante:" + p.getRemainingTime() + ")");
                totalProcesosEnColas++;
            }
        }

        System.out.println("   Total procesos en colas: " + totalProcesosEnColas);
        System.out.println("   Total procesos cargados: " + todosLosProcesos.size());
        System.out.println("   Total procesos completados: " + procesosCompletados.size());

        // Verificar duplicados por etiqueta
        Set<String> etiquetasVistas = new HashSet<>();
        boolean hayDuplicados = false;
        for (Cola cola : colas) {
            for (Proceso p : cola.getProcesos()) {
                if (etiquetasVistas.contains(p.getEtiqueta())) {
                    System.out.println("   ⚠️ DUPLICADO DETECTADO: " + p.getEtiqueta());
                    hayDuplicados = true;
                }
                etiquetasVistas.add(p.getEtiqueta());
            }
        }

        if (!hayDuplicados) {
            System.out.println("   ✅ No se detectaron duplicados en las colas");
        }
        System.out.println();
    }

    // Getters para pruebas
    public List<Cola> getColas() { return colas; }
    public List<Proceso> getProcesosCompletados() { return procesosCompletados; }
    public List<Proceso> getTodosLosProcesos() { return todosLosProcesos; }
}