package ar.edu.utn.frbb.tup.persistence;

import ar.edu.utn.frbb.tup.model.Alumno;
import ar.edu.utn.frbb.tup.model.Asignatura;
import ar.edu.utn.frbb.tup.model.EstadoAsignatura;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class AsignaturaDaoMemoryImpl implements AsignaturaDao {

    private static final String CSV_FILE_PATH = "C:/Users/Felipe/IdeaProjects/LABORATORIO3/src/main/java/ar/edu/utn/frbb/tup/persistence/dataCSV/asignaturaDATA.csv";

    public void guardarAsignatura(Asignatura asignatura) {
        FileWriter fileWriter = null;
        PrintWriter printWriter = null;

        try {
            // Abrir el archivo en modo de agregar (append = true)
            fileWriter = new FileWriter(CSV_FILE_PATH, true);
            printWriter = new PrintWriter(fileWriter);

            // Escribir los atributos del alumno en formato CSV
            printWriter.println(
                            asignatura.getId() + "," +
                            asignatura.getIdalumno() + "," +
                            asignatura.getIdmateria() + "," +
                            asignatura.getNota()+ "," +
                            asignatura.getEstado()


            );

            System.out.println("Asignatura guardado correctamente en el archivo CSV.");
        } catch (IOException e) {
            System.err.println("Error al escribir en el archivo CSV: " + e.getMessage());
        } finally {
            // Cerrar los recursos
            if (printWriter != null) {
                printWriter.close();
            }
            if (fileWriter != null) {
                try {
                    fileWriter.close();
                } catch (IOException e) {
                    System.err.println("Error al cerrar el archivo: " + e.getMessage());
                }
            }
        }
    }

    @Override
    public List<Asignatura> buscarAsignaturas() {
        List<Asignatura> asignaturas = new ArrayList<>();
        BufferedReader bufferedReader = null;

        try {
            bufferedReader = new BufferedReader(new FileReader(CSV_FILE_PATH));
            String linea;

            while ((linea = bufferedReader.readLine()) != null) {
                String[] datos = linea.split(",");

                if (datos.length < 4) {
                    System.err.println("Línea con formato incorrecto: " + linea);
                    continue; // Saltar líneas con formato incorrecto
                }

                try {
                    long id= Long.parseLong(datos[0].trim());
                    long idalumno = Long.parseLong(datos[1].trim());
                    long idmateria = Long.parseLong(datos[2].trim());
                    int nota = Integer.parseInt(datos[3].trim());

                    // Convertir el String del archivo CSV a EstadoAsignatura usando valueOf
                    EstadoAsignatura estado = EstadoAsignatura.valueOf(datos[4].trim());

                    Asignatura asignatura = new Asignatura(id,estado, nota, idalumno, idmateria);
                    asignaturas.add(asignatura);
                } catch (NumberFormatException e) {
                    System.err.println("Error al parsear números en la línea: " + linea);
                    // Puedes decidir si continuar o lanzar una excepción
                } catch (IllegalArgumentException e) {
                    System.err.println("Estado de asignatura inválido: " + datos[4].trim());
                    // Saltar líneas con estados inválidos o manejar según tu lógica
                }
            }
        } catch (IOException e) {
            System.err.println("Error al leer el archivo CSV: " + e.getMessage());
            // Dependiendo de tu lógica, podrías lanzar una excepción aquí
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    System.err.println("Error al cerrar el archivo: " + e.getMessage());
                }
            }
        }

        return asignaturas;
    }



    @Override
    public Asignatura buscarAsignaturaporId(long id) {
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(CSV_FILE_PATH))) {
            String linea;
            while ((linea = bufferedReader.readLine()) != null) {
                String[] datos = linea.split(","); //
                if (datos.length < 5) {
                    System.err.println("Línea con formato incorrecto: " + linea);
                    continue; // Salta la línea con formato incorrecto
                }
                try {
                    long idasignatura = Long.parseLong(datos[0].trim());
                    long idalumno = Long.parseLong(datos[1].trim());
                    long idmateria = Long.parseLong(datos[2].trim());
                    int nota = Integer.parseInt(datos[3].trim());

                    // Convertir el String del archivo CSV a EstadoAsignatura usando valueOf
                    EstadoAsignatura estado = EstadoAsignatura.valueOf(datos[4].trim());


                    if (idasignatura ==id) {
                        return new Asignatura(estado,nota,idalumno,idmateria);
                    }
                } catch (NumberFormatException e) {
                    System.err.println("Error al parsear número: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Error al leer el archivo CSV: " + e.getMessage());
        }
        return null;

    }

    @Override
    public Asignatura modificarAsignatura(Asignatura asignatura) {
        File inputFile = new File(CSV_FILE_PATH);
        File tempFile = new File("tempFile.csv");
        BufferedReader bufferedReader = null;
        PrintWriter printWriter = null;
        Asignatura asignaturamodificado = null;

        try {
            bufferedReader = new BufferedReader(new FileReader(inputFile));
            printWriter = new PrintWriter(new FileWriter(tempFile));
            String linea;

            while ((linea = bufferedReader.readLine()) != null) {
                String[] datos = linea.split(",");
                if (datos.length < 5) {
                    // Si la línea no tiene el formato correcto, la copiamos tal cual
                    printWriter.println(linea);
                    continue;
                }

                long id= Long.parseLong(datos[0]);

                if (id == asignatura.getId()) {
                    // Reemplazamos la línea con los datos actualizados del alumno
                    printWriter.println(
                            asignatura.getId() + "," +
                                    asignatura.getIdalumno() + "," +
                                    asignatura.getIdmateria() + "," +
                                    asignatura.getNota()+ "," +
                                    asignatura.getEstado()

                    );
                    asignaturamodificado = asignatura;
                } else {
                    // Escribimos la línea existente sin modificaciones
                    printWriter.println(linea);
                }
            }
        } catch (IOException e) {
            System.err.println("Error al procesar el archivo CSV: " + e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al modificar la asignatura.");
        } finally {
            try {
                if (bufferedReader != null) bufferedReader.close();
                if (printWriter != null) printWriter.close();
            } catch (IOException e) {
                System.err.println("Error al cerrar los recursos: " + e.getMessage());
            }
        }

        // Reemplazamos el archivo original con el temporal
        if (inputFile.delete()) {
            if (!tempFile.renameTo(inputFile)) {
                System.err.println("No se pudo renombrar el archivo temporal.");
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al renombrar el archivo temporal.");
            }
        } else {
            System.err.println("No se pudo eliminar el archivo original.");
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al eliminar el archivo original.");
        }

        if (asignaturamodificado != null) {
            System.out.println("asignatura modificado exitosamente.");
            return asignaturamodificado;
        } else {
            System.out.println("No se encontró un asignatura con el ID proporcionado.");
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "asignatura no encontrado.");
        }
    }

    @Override
    public Asignatura borrarAsignaturaporid(long id) {
        File inputFile = new File(CSV_FILE_PATH);
        BufferedReader bufferedReader = null;
        File tempFile = new File("tempFile.csv");
        PrintWriter printWriter = null;
        Asignatura asignaturaEliminada = null;

        try {
            bufferedReader = new BufferedReader(new FileReader(inputFile));
            printWriter = new PrintWriter(new FileWriter(tempFile));
            String linea;

            while ((linea = bufferedReader.readLine()) != null) {
                String[] datos = linea.split(",");
                long idasignatura = Long.parseLong(datos[0].trim());
                long idalumno = Long.parseLong(datos[1].trim());
                long idmateria = Long.parseLong(datos[2].trim());
                int nota = Integer.parseInt(datos[3].trim());

                // Convertir el String del archivo CSV a EstadoAsignatura usando valueOf
                EstadoAsignatura estado = EstadoAsignatura.valueOf(datos[4].trim());

                // Si el id de la asignatura no coincide, escribir la línea en el archivo temporal
                if (idasignatura != id) {
                    printWriter.println(linea);
                } else {
                    // Si coincide, guardar la asignatura eliminada
                    asignaturaEliminada = new Asignatura(estado,nota, idalumno, idmateria);
                }
            }

        } catch (IOException e) {
            System.err.println("Error al leer el archivo CSV: " + e.getMessage());
        } finally {
            try {
                if (bufferedReader != null) bufferedReader.close();
                if (printWriter != null) printWriter.close();
            } catch (IOException e) {
                System.err.println("Error al cerrar los recursos: " + e.getMessage());
            }
        }

        if (asignaturaEliminada != null) {
            if (!inputFile.delete()) {
                System.out.println("No se pudo eliminar el archivo original");
                return null;
            }
            if (!tempFile.renameTo(inputFile)) {
                System.out.println("No se pudo renombrar el archivo temporal");
                return null;
            }
            System.out.println("Asignatura eliminada exitosamente!");

            return asignaturaEliminada;
        } else {
            System.out.println("No existe asignatura con el id proporcionado: " + id);
            return null;
        }
    }

    @Override
    public int obtenerUltimoId() {
        BufferedReader bufferedReader = null;
        int ultimoId = 0;

        try {
            bufferedReader = new BufferedReader(new FileReader(CSV_FILE_PATH));
            String linea;

            while ((linea = bufferedReader.readLine()) != null) {
                String[] datos = linea.split(",");

                if (datos.length > 0) {
                    try {
                        int idActual = Integer.parseInt(datos[0].trim());
                        // Guardar el ID más alto encontrado
                        if (idActual > ultimoId) {
                            ultimoId = idActual;
                        }
                    } catch (NumberFormatException e) {
                        System.err.println("Error al parsear el ID en la línea: " + linea);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error al leer el archivo CSV: " + e.getMessage());
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    System.err.println("Error al cerrar el archivo: " + e.getMessage());
                }
            }
        }

        return ultimoId;
    }
}
