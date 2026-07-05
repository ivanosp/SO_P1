import scheduler.PlanificadorMLQ;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        // Verificar argumentos
        if (args.length < 2) {
            System.out.println("Uso: java Main <archivo_entrada> <archivo_salida>");
            System.out.println("Ejemplo: java Main inputs/mlq003.txt outputs/resultado003.txt");
            return;
        }

        String archivoEntrada = args[0];
        String archivoSalida = args[1];

        try {
            PlanificadorMLQ planificador = new PlanificadorMLQ();
            planificador.configurarColas();
            planificador.cargarProcesos(archivoEntrada);
            planificador.ejecutar();
            planificador.generarSalida(archivoSalida);

            System.out.println("✅ Planificación completada. Resultados guardados en: " + archivoSalida);
        } catch (IOException e) {
            System.err.println("❌ Error al procesar archivos: " + e.getMessage());
            e.printStackTrace();
        }
    }
}