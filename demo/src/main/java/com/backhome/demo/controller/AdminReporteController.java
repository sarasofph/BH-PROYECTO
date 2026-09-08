package com.backhome.demo.controller;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.backhome.demo.model.Animal;
import com.backhome.demo.model.Consejo;
import com.backhome.demo.model.Donacion;
import com.backhome.demo.model.Persona;
import com.backhome.demo.model.Refugio;
import com.backhome.demo.model.Seguimiento;
import com.backhome.demo.repository.AnimalRepository;
import com.backhome.demo.repository.ConsejoRepository;
import com.backhome.demo.repository.DonacionRepository;
import com.backhome.demo.repository.PersonaRepository;
import com.backhome.demo.repository.RefugioRepository;
import com.backhome.demo.repository.SeguimientoRepository;

@Controller
@RequestMapping("/admin/reportes")
@PreAuthorize("hasRole('ADMIN')")
public class AdminReporteController {

    private static final PDRectangle A4 = PDRectangle.A4;

    private static final float ANCHO = A4.getWidth();
    private static final float ALTO = A4.getHeight();
    private static final float MARGEN = 42f;

    private static final Color ROSA = new Color(232, 93, 140);
    private static final Color ROSA_OSCURO = new Color(191, 62, 108);
    private static final Color ROSA_SUAVE = new Color(253, 232, 240);

    private static final Color TEXTO = new Color(55, 49, 53);
    private static final Color GRIS = new Color(112, 105, 109);
    private static final Color GRIS_CLARO = new Color(150, 143, 148);

    private static final Color FONDO = new Color(249, 247, 248);
    private static final Color BLANCO = Color.WHITE;
    private static final Color BORDE = new Color(232, 225, 229);

    private final PersonaRepository personaRepository;
    private final RefugioRepository refugioRepository;
    private final AnimalRepository animalRepository;
    private final SeguimientoRepository seguimientoRepository;
    private final DonacionRepository donacionRepository;
    private final ConsejoRepository consejoRepository;

    private final PDType1Font normal =
            new PDType1Font(Standard14Fonts.FontName.HELVETICA);

    private final PDType1Font bold =
            new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);

    private final DateTimeFormatter fechaHora =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final DateTimeFormatter fecha =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public AdminReporteController(
            PersonaRepository personaRepository,
            RefugioRepository refugioRepository,
            AnimalRepository animalRepository,
            SeguimientoRepository seguimientoRepository,
            DonacionRepository donacionRepository,
            ConsejoRepository consejoRepository) {

        this.personaRepository = personaRepository;
        this.refugioRepository = refugioRepository;
        this.animalRepository = animalRepository;
        this.seguimientoRepository = seguimientoRepository;
        this.donacionRepository = donacionRepository;
        this.consejoRepository = consejoRepository;
    }

    // =========================================================
    // VISTA
    // =========================================================

    @GetMapping
    public String reportes() {
        return "admin/reportes";
    }

    // =========================================================
    // REPORTE PERSONALIZADO
    // =========================================================

    @GetMapping("/personalizado/pdf")
    public ResponseEntity<byte[]> generarReportePersonalizado(
            @RequestParam(required = false) List<String> tipos)
            throws IOException {

        if (tipos == null || tipos.isEmpty()) {
            tipos = new ArrayList<>();
            tipos.add("usuarios");
            tipos.add("refugios");
            tipos.add("animales");
            tipos.add("seguimientos");
            tipos.add("donaciones");
            tipos.add("consejos");
        }

        List<String> permitidos = Arrays.asList(
                "usuarios",
                "refugios",
                "animales",
                "seguimientos",
                "donaciones",
                "consejos"
        );

        List<String> seleccionados = new ArrayList<>();

        for (String tipo : tipos) {

            if (permitidos.contains(tipo)
                    && !seleccionados.contains(tipo)) {

                seleccionados.add(tipo);
            }
        }

        if (seleccionados.isEmpty()) {
            seleccionados.add("usuarios");
        }

        byte[] pdf =
                crearReportePersonalizado(seleccionados);

        return respuestaPdf(
                pdf,
                "reporte-backhome-personalizado.pdf"
        );
    }

    // =========================================================
    // REPORTE GENERAL ANTERIOR
    // =========================================================

    @GetMapping("/general/pdf")
    public ResponseEntity<byte[]> generarReporteGeneral()
            throws IOException {

        List<String> todos = Arrays.asList(
                "usuarios",
                "refugios",
                "animales",
                "seguimientos",
                "donaciones",
                "consejos"
        );

        byte[] pdf =
                crearReportePersonalizado(todos);

        return respuestaPdf(
                pdf,
                "reporte-general-backhome.pdf"
        );
    }

    // =========================================================
    // REPORTE DONACIONES
    // =========================================================

    @GetMapping("/donaciones/pdf")
    public ResponseEntity<byte[]> generarReporteDonaciones()
            throws IOException {

        byte[] pdf =
                crearReportePersonalizado(
                        Collections.singletonList("donaciones")
                );

        return respuestaPdf(
                pdf,
                "reporte-donaciones-backhome.pdf"
        );
    }

    // =========================================================
    // CONSTRUIR PDF
    // =========================================================

    private byte[] crearReportePersonalizado(
            List<String> seleccionados)
            throws IOException {

        try (PDDocument documento =
                     new PDDocument()) {

            // PORTADA
            PDPage portada = new PDPage(A4);
            documento.addPage(portada);

            try (PDPageContentStream contenido =
                         new PDPageContentStream(
                                 documento,
                                 portada)) {

                dibujarFondo(contenido);

                dibujarEncabezado(
                        documento,
                        contenido,
                        "REPORTE ADMINISTRATIVO",
                        1,
                        1
                );

                float y = 650;

                escribirTexto(
                        contenido,
                        "Reporte personalizado",
                        MARGEN,
                        y,
                        24,
                        bold,
                        TEXTO
                );

                escribirTexto(
                        contenido,
                        "Información seleccionada del sistema BackHome",
                        MARGEN,
                        y - 30,
                        11,
                        normal,
                        GRIS
                );

                y -= 95;

                contenido.setNonStrokingColor(
                        BLANCO
                );

                contenido.addRect(
                        MARGEN,
                        y - 215,
                        ANCHO - (MARGEN * 2),
                        215
                );

                contenido.fill();

                contenido.setStrokingColor(
                        BORDE
                );

                contenido.addRect(
                        MARGEN,
                        y - 215,
                        ANCHO - (MARGEN * 2),
                        215
                );

                contenido.stroke();

                escribirTexto(
                        contenido,
                        "CONTENIDO DEL REPORTE",
                        MARGEN + 20,
                        y - 28,
                        10,
                        bold,
                        ROSA_OSCURO
                );

                float listaY = y - 55;

                for (String tipo : seleccionados) {

                    escribirTexto(
                            contenido,
                            "✓",
                            MARGEN + 22,
                            listaY,
                            11,
                            bold,
                            ROSA
                    );

                    escribirTexto(
                            contenido,
                            nombreTipo(tipo),
                            MARGEN + 42,
                            listaY,
                            10,
                            normal,
                            TEXTO
                    );

                    listaY -= 27;
                }

                y -= 265;

                escribirTexto(
                        contenido,
                        "Fecha de generación",
                        MARGEN,
                        y,
                        8,
                        bold,
                        GRIS
                );

                escribirTexto(
                        contenido,
                        LocalDateTime.now()
                                .format(fechaHora),
                        MARGEN,
                        y - 18,
                        11,
                        normal,
                        TEXTO
                );

                escribirTexto(
                        contenido,
                        "Documento generado automáticamente desde el panel administrativo.",
                        MARGEN,
                        y - 50,
                        8,
                        normal,
                        GRIS
                );

                dibujarPiePagina(
                        contenido,
                        1,
                        1
                );
            }

            // SECCIONES
            for (String tipo : seleccionados) {

                switch (tipo) {

                    case "usuarios":
                        agregarUsuarios(
                                documento
                        );
                        break;

                    case "refugios":
                        agregarRefugios(
                                documento
                        );
                        break;

                    case "animales":
                        agregarAnimales(
                                documento
                        );
                        break;

                    case "seguimientos":
                        agregarSeguimientos(
                                documento
                        );
                        break;

                    case "donaciones":
                        agregarDonaciones(
                                documento
                        );
                        break;

                    case "consejos":
                        agregarConsejos(
                                documento
                        );
                        break;

                    default:
                        break;
                }
            }

            // Recalcular números de página
            int totalPaginas =
                    documento.getNumberOfPages();

            for (int i = 0;
                    i < totalPaginas;
                    i++) {

                PDPage pagina =
                        documento.getPage(i);

                try (PDPageContentStream contenido =
                             new PDPageContentStream(
                                     documento,
                                     pagina,
                                     PDPageContentStream.AppendMode.APPEND,
                                     true,
                                     true)) {

                    dibujarNumeroPagina(
                            contenido,
                            i + 1,
                            totalPaginas
                    );
                }
            }

            ByteArrayOutputStream salida =
                    new ByteArrayOutputStream();

            documento.save(salida);

            return salida.toByteArray();
        }
    }

    // =========================================================
    // USUARIOS
    // =========================================================

    private void agregarUsuarios(
            PDDocument documento)
            throws IOException {

        List<Persona> personas =
                personaRepository
                        .findAllByOrderByIdPersonaDesc();

        nuevaPaginaConTitulo(
                documento,
                "USUARIOS",
                "Listado detallado de personas registradas"
        );

        int pagina =
                documento.getNumberOfPages() - 1;

        PDPage page =
                documento.getPage(pagina);

        float y = 650;

        try (PDPageContentStream contenido =
                     new PDPageContentStream(
                             documento,
                             page,
                             PDPageContentStream.AppendMode.APPEND,
                             true,
                             true)) {

            dibujarTablaCabecera(
                    contenido,
                    y,
                    new String[]{
                            "ID",
                            "NOMBRE",
                            "DOCUMENTO",
                            "EMAIL",
                            "TELÉFONO",
                            "ESTADO"
                    },
                    new float[]{
                            35, 120, 80, 130, 80, 70
                    }
            );

            y -= 28;

            for (Persona persona : personas) {

                if (y < 75) {

                    contenido.close();

                    page =
                            nuevaPaginaConTitulo(
                                    documento,
                                    "USUARIOS",
                                    "Continuación del listado"
                            );

                    y = 650;

                    try (PDPageContentStream nuevo =
                                 new PDPageContentStream(
                                         documento,
                                         page)) {

                        dibujarTablaCabecera(
                                nuevo,
                                y,
                                new String[]{
                                        "ID",
                                        "NOMBRE",
                                        "DOCUMENTO",
                                        "EMAIL",
                                        "TELÉFONO",
                                        "ESTADO"
                                },
                                new float[]{
                                        35, 120, 80, 130, 80, 70
                                }
                        );

                        y -= 28;

                        y = dibujarFilaUsuario(
                                nuevo,
                                persona,
                                y
                        );
                    }

                    continue;
                }

                y = dibujarFilaUsuario(
                        contenido,
                        persona,
                        y
                );
            }
        }
    }

    private float dibujarFilaUsuario(
            PDPageContentStream contenido,
            Persona persona,
            float y)
            throws IOException {

        float x = MARGEN;

        String nombre =
                ((persona.getPrimerNombre() == null
                        ? ""
                        : persona.getPrimerNombre())
                + " "
                + (persona.getPrimerApellido() == null
                        ? ""
                        : persona.getPrimerApellido()))
                .trim();

        if (nombre.isBlank()) {
            nombre = "Sin nombre";
        }

        String estado =
                persona.getEstado() == null
                        ? "-"
                        : persona.getEstado().toString();

        String[] valores = {
                String.valueOf(persona.getIdPersona()),
                cortar(nombre, 21),
                cortar(persona.getNumeroDocumento(), 14),
                cortar(persona.getEmail(), 23),
                cortar(persona.getNumeroTel(), 14),
                cortar(estado, 12)
        };

        float[] anchos = {
                35, 120, 80, 130, 80, 70
        };

        dibujarFila(
                contenido,
                x,
                y,
                valores,
                anchos
        );

        return y - 35;
    }

    // =========================================================
    // REFUGIOS
    // =========================================================

    private void agregarRefugios(
            PDDocument documento)
            throws IOException {

        List<Refugio> refugios =
                refugioRepository
                        .findAllByOrderByIdRefugioDesc();

        PDPage page =
                nuevaPaginaConTitulo(
                        documento,
                        "REFUGIOS",
                        "Listado detallado de refugios registrados"
                );

        float y = 650;

        try (PDPageContentStream contenido =
                     new PDPageContentStream(
                             documento,
                             page)) {

            dibujarTablaCabecera(
                    contenido,
                    y,
                    new String[]{
                            "ID",
                            "NOMBRE",
                            "DIRECCIÓN",
                            "TELÉFONO",
                            "EMAIL",
                            "LOCALIDAD"
                    },
                    new float[]{
                            35, 105, 125, 75, 115, 90
                    }
            );

            y -= 28;

            for (Refugio refugio : refugios) {

                if (y < 75) {

                    contenido.close();

                    page =
                            nuevaPaginaConTitulo(
                                    documento,
                                    "REFUGIOS",
                                    "Continuación del listado"
                            );

                    y = 650;

                    try (PDPageContentStream nuevo =
                                 new PDPageContentStream(
                                         documento,
                                         page)) {

                        dibujarTablaCabecera(
                                nuevo,
                                y,
                                new String[]{
                                        "ID",
                                        "NOMBRE",
                                        "DIRECCIÓN",
                                        "TELÉFONO",
                                        "EMAIL",
                                        "LOCALIDAD"
                                },
                                new float[]{
                                        35, 105, 125, 75, 115, 90
                                }
                        );

                        y -= 28;

                        y = dibujarFilaRefugio(
                                nuevo,
                                refugio,
                                y
                        );
                    }

                    continue;
                }

                y = dibujarFilaRefugio(
                        contenido,
                        refugio,
                        y
                );
            }
        }
    }

    private float dibujarFilaRefugio(
            PDPageContentStream contenido,
            Refugio refugio,
            float y)
            throws IOException {

        String localidad = "-";

        if (refugio.getLocalidad() != null) {
            localidad =
                    refugio.getLocalidad().getNombre();
        }

        String[] valores = {
                String.valueOf(
                        refugio.getIdRefugio()
                ),
                cortar(
                        refugio.getNombre(),
                        18
                ),
                cortar(
                        refugio.getDireccion(),
                        22
                ),
                cortar(
                        refugio.getTelefono(),
                        13
                ),
                cortar(
                        refugio.getEmail(),
                        21
                ),
                cortar(
                        localidad,
                        16
                )
        };

        float[] anchos = {
                35, 105, 125, 75, 115, 90
        };

        dibujarFila(
                contenido,
                MARGEN,
                y,
                valores,
                anchos
        );

        return y - 35;
    }

    // =========================================================
    // ANIMALES
    // =========================================================

    private void agregarAnimales(
            PDDocument documento)
            throws IOException {

        List<Animal> animales =
                animalRepository
                        .findAllByOrderByIdAnimalDesc();

        PDPage page =
                nuevaPaginaConTitulo(
                        documento,
                        "ANIMALES",
                        "Listado detallado de animales registrados"
                );

        float y = 650;

        try (PDPageContentStream contenido =
                     new PDPageContentStream(
                             documento,
                             page)) {

            dibujarTablaCabecera(
                    contenido,
                    y,
                    new String[]{
                            "ID",
                            "NOMBRE",
                            "SEXO",
                            "TAMAÑO",
                            "COLOR",
                            "DESCRIPCIÓN"
                    },
                    new float[]{
                            40, 105, 75, 75, 95, 155
                    }
            );

            y -= 28;

            for (Animal animal : animales) {

                if (y < 75) {

                    contenido.close();

                    page =
                            nuevaPaginaConTitulo(
                                    documento,
                                    "ANIMALES",
                                    "Continuación del listado"
                            );

                    y = 650;

                    try (PDPageContentStream nuevo =
                                 new PDPageContentStream(
                                         documento,
                                         page)) {

                        dibujarTablaCabecera(
                                nuevo,
                                y,
                                new String[]{
                                        "ID",
                                        "NOMBRE",
                                        "SEXO",
                                        "TAMAÑO",
                                        "COLOR",
                                        "DESCRIPCIÓN"
                                },
                                new float[]{
                                        40, 105, 75, 75, 95, 155
                                }
                        );

                        y -= 28;

                        y = dibujarFilaAnimal(
                                nuevo,
                                animal,
                                y
                        );
                    }

                    continue;
                }

                y = dibujarFilaAnimal(
                        contenido,
                        animal,
                        y
                );
            }
        }
    }

    private float dibujarFilaAnimal(
            PDPageContentStream contenido,
            Animal animal,
            float y)
            throws IOException {

        String sexo =
                animal.getSexo() == null
                        ? "-"
                        : animal.getSexo().toString();

        String tamano =
                animal.getTamano() == null
                        ? "-"
                        : animal.getTamano().toString();

        String[] valores = {
                String.valueOf(
                        animal.getIdAnimal()
                ),
                cortar(
                        animal.getNombre(),
                        17
                ),
                cortar(sexo, 12),
                cortar(tamano, 12),
                cortar(animal.getColor(), 15),
                cortar(animal.getDescripcion(), 25)
        };

        float[] anchos = {
                40, 105, 75, 75, 95, 155
        };

        dibujarFila(
                contenido,
                MARGEN,
                y,
                valores,
                anchos
        );

        return y - 35;
    }

    // =========================================================
    // SEGUIMIENTOS
    // =========================================================

    private void agregarSeguimientos(
            PDDocument documento)
            throws IOException {

        List<Seguimiento> seguimientos =
                seguimientoRepository
                        .findAllByOrderByIdSeguimientoDesc();

        PDPage page =
                nuevaPaginaConTitulo(
                        documento,
                        "SEGUIMIENTOS",
                        "Listado detallado de seguimientos"
                );

        float y = 650;

        try (PDPageContentStream contenido =
                     new PDPageContentStream(
                             documento,
                             page)) {

            dibujarTablaCabecera(
                    contenido,
                    y,
                    new String[]{
                            "ID",
                            "TÍTULO",
                            "TIPO",
                            "ESTADO",
                            "MODERACIÓN",
                            "FECHA"
                    },
                    new float[]{
                            35, 150, 85, 80, 105, 90
                    }
            );

            y -= 28;

            for (Seguimiento seguimiento : seguimientos) {

                if (y < 75) {

                    contenido.close();

                    page =
                            nuevaPaginaConTitulo(
                                    documento,
                                    "SEGUIMIENTOS",
                                    "Continuación del listado"
                            );

                    y = 650;

                    try (PDPageContentStream nuevo =
                                 new PDPageContentStream(
                                         documento,
                                         page)) {

                        dibujarTablaCabecera(
                                nuevo,
                                y,
                                new String[]{
                                        "ID",
                                        "TÍTULO",
                                        "TIPO",
                                        "ESTADO",
                                        "MODERACIÓN",
                                        "FECHA"
                                },
                                new float[]{
                                        35, 150, 85, 80, 105, 90
                                }
                        );

                        y -= 28;

                        y = dibujarFilaSeguimiento(
                                nuevo,
                                seguimiento,
                                y
                        );
                    }

                    continue;
                }

                y = dibujarFilaSeguimiento(
                        contenido,
                        seguimiento,
                        y
                );
            }
        }
    }

    private float dibujarFilaSeguimiento(
            PDPageContentStream contenido,
            Seguimiento seguimiento,
            float y)
            throws IOException {

        String tipo =
                seguimiento.getTipoSeguimiento() == null
                        ? "-"
                        : seguimiento.getTipoSeguimiento()
                                .toString();

        String estado =
                seguimiento.getEstadoSeguimiento() == null
                        ? "-"
                        : seguimiento.getEstadoSeguimiento()
                                .toString();

        String moderacion =
                seguimiento.getEstadoModeracion() == null
                        ? "-"
                        : seguimiento.getEstadoModeracion()
                                .toString();

        String fechaPublicacion =
                seguimiento.getFechaPublicacion() == null
                        ? "-"
                        : seguimiento.getFechaPublicacion()
                                .format(fecha);

        String[] valores = {
                String.valueOf(
                        seguimiento.getIdSeguimiento()
                ),
                cortar(
                        seguimiento.getTitulo(),
                        25
                ),
                cortar(tipo, 14),
                cortar(estado, 13),
                cortar(moderacion, 17),
                fechaPublicacion
        };

        float[] anchos = {
                35, 150, 85, 80, 105, 90
        };

        dibujarFila(
                contenido,
                MARGEN,
                y,
                valores,
                anchos
        );

        return y - 35;
    }

    // =========================================================
    // DONACIONES
    // =========================================================

    private void agregarDonaciones(
            PDDocument documento)
            throws IOException {

        List<Donacion> donaciones =
                donacionRepository
                        .findAllByOrderByIdDonacionDesc();

        BigDecimal total =
                donaciones.stream()
                        .map(Donacion::getMonto)
                        .filter(monto -> monto != null)
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        BigDecimal promedio =
                donaciones.isEmpty()
                        ? BigDecimal.ZERO
                        : total.divide(
                                BigDecimal.valueOf(
                                        donaciones.size()
                                ),
                                2,
                                RoundingMode.HALF_UP
                        );

        PDPage resumen =
                nuevaPaginaConTitulo(
                        documento,
                        "DONACIONES",
                        "Detalle y resumen financiero"
                );

        try (PDPageContentStream contenido =
                     new PDPageContentStream(
                             documento,
                             resumen)) {

            dibujarKpi(
                    contenido,
                    MARGEN,
                    600,
                    150,
                    75,
                    "DONACIONES",
                    String.valueOf(
                            donaciones.size()
                    )
            );

            dibujarKpi(
                    contenido,
                    MARGEN + 165,
                    600,
                    175,
                    75,
                    "TOTAL RECIBIDO",
                    formatoDinero(total)
            );

            dibujarKpi(
                    contenido,
                    MARGEN + 355,
                    600,
                    160,
                    75,
                    "PROMEDIO",
                    formatoDinero(promedio)
            );

            escribirTexto(
                    contenido,
                    "Detalle de donaciones",
                    MARGEN,
                    555,
                    15,
                    bold,
                    TEXTO
            );

            escribirTexto(
                    contenido,
                    "Cada registro corresponde a una donación almacenada en el sistema.",
                    MARGEN,
                    537,
                    8,
                    normal,
                    GRIS
            );
        }

        PDPage page =
                nuevaPaginaConTitulo(
                        documento,
                        "DONACIONES",
                        "Listado detallado"
                );

        float y = 650;

        try (PDPageContentStream contenido =
                     new PDPageContentStream(
                             documento,
                             page)) {

            dibujarTablaCabecera(
                    contenido,
                    y,
                    new String[]{
                            "ID",
                            "DONANTE",
                            "CORREO",
                            "MONTO",
                            "FECHA",
                            "MENSAJE"
                    },
                    new float[]{
                            35, 120, 120, 70, 75, 105
                    }
            );

            y -= 28;

            for (Donacion donacion : donaciones) {

                if (y < 75) {

                    contenido.close();

                    page =
                            nuevaPaginaConTitulo(
                                    documento,
                                    "DONACIONES",
                                    "Continuación del listado"
                            );

                    y = 650;

                    try (PDPageContentStream nuevo =
                                 new PDPageContentStream(
                                         documento,
                                         page)) {

                        dibujarTablaCabecera(
                                nuevo,
                                y,
                                new String[]{
                                        "ID",
                                        "DONANTE",
                                        "CORREO",
                                        "MONTO",
                                        "FECHA",
                                        "MENSAJE"
                                },
                                new float[]{
                                        35, 120, 120, 70, 75, 105
                                }
                        );

                        y -= 28;

                        y = dibujarFilaDonacion(
                                nuevo,
                                donacion,
                                y
                        );
                    }

                    continue;
                }

                y = dibujarFilaDonacion(
                        contenido,
                        donacion,
                        y
                );
            }
        }
    }

    private float dibujarFilaDonacion(
            PDPageContentStream contenido,
            Donacion donacion,
            float y)
            throws IOException {

        String nombre = "No disponible";
        String correo = "No disponible";

        try {

            if (donacion.getCliente() != null
                    && donacion.getCliente().getPersona() != null) {

                String n =
                        donacion.getCliente()
                                .getPersona()
                                .getPrimerNombre();

                String a =
                        donacion.getCliente()
                                .getPersona()
                                .getPrimerApellido();

                nombre =
                        ((n == null ? "" : n)
                                + " "
                                + (a == null ? "" : a))
                                .trim();

                correo =
                        donacion.getCliente()
                                .getPersona()
                                .getEmail();

                if (nombre.isBlank()) {
                    nombre = "No disponible";
                }

                if (correo == null
                        || correo.isBlank()) {

                    correo = "No disponible";
                }
            }

        } catch (Exception e) {
            nombre = "No disponible";
            correo = "No disponible";
        }

        String monto =
                donacion.getMonto() == null
                        ? "$ 0.00"
                        : formatoDinero(
                                donacion.getMonto()
                        );

        String fechaDonacion =
                donacion.getFechaDonacion() == null
                        ? "-"
                        : donacion.getFechaDonacion()
                                .format(fecha);

        String mensaje =
                donacion.getMensaje();

        if (mensaje == null
                || mensaje.isBlank()) {

            mensaje = "Sin mensaje";
        }

        String[] valores = {
                String.valueOf(
                        donacion.getIdDonacion()
                ),
                cortar(nombre, 20),
                cortar(correo, 21),
                monto,
                fechaDonacion,
                cortar(mensaje, 19)
        };

        float[] anchos = {
                35, 120, 120, 70, 75, 105
        };

        dibujarFila(
                contenido,
                MARGEN,
                y,
                valores,
                anchos
        );

        return y - 35;
    }

    // =========================================================
    // CONSEJOS
    // =========================================================

    private void agregarConsejos(
            PDDocument documento)
            throws IOException {

        List<Consejo> consejos =
                consejoRepository
                        .findAllByOrderByIdDesc();

        PDPage page =
                nuevaPaginaConTitulo(
                        documento,
                        "CONSEJOS",
                        "Consejos y recomendaciones registrados"
                );

        float y = 650;

        try (PDPageContentStream contenido =
                     new PDPageContentStream(
                             documento,
                             page)) {

            if (consejos.isEmpty()) {

                escribirTexto(
                        contenido,
                        "No hay consejos registrados.",
                        MARGEN,
                        y,
                        11,
                        normal,
                        GRIS
                );

                return;
            }

            for (Consejo consejo : consejos) {

                if (y < 130) {

                    contenido.close();

                    page =
                            nuevaPaginaConTitulo(
                                    documento,
                                    "CONSEJOS",
                                    "Continuación del listado"
                            );

                    y = 650;

                    try (PDPageContentStream nuevo =
                                 new PDPageContentStream(
                                         documento,
                                         page)) {

                        y = dibujarConsejo(
                                nuevo,
                                consejo,
                                y
                        );
                    }

                    continue;
                }

                y = dibujarConsejo(
                        contenido,
                        consejo,
                        y
                );
            }
        }
    }

    private float dibujarConsejo(
            PDPageContentStream contenido,
            Consejo consejo,
            float y)
            throws IOException {

        float alto = 85;
        float ancho =
                ANCHO - (MARGEN * 2);

        contenido.setNonStrokingColor(
                BLANCO
        );

        contenido.addRect(
                MARGEN,
                y - alto,
                ancho,
                alto
        );

        contenido.fill();

        contenido.setStrokingColor(
                BORDE
        );

        contenido.addRect(
                MARGEN,
                y - alto,
                ancho,
                alto
        );

        contenido.stroke();

        contenido.setNonStrokingColor(
                ROSA
        );

        contenido.addRect(
                MARGEN,
                y - alto,
                5,
                alto
        );

        contenido.fill();

        escribirTexto(
                contenido,
                limpiarTexto(
                        consejo.getTitulo()
                ),
                MARGEN + 16,
                y - 23,
                11,
                bold,
                TEXTO
        );

        List<String> lineas =
                dividirTexto(
                        consejo.getDescripcion(),
                        105
                );

        float textoY = y - 43;

        for (int i = 0;
                i < Math.min(2, lineas.size());
                i++) {

            escribirTexto(
                    contenido,
                    lineas.get(i),
                    MARGEN + 16,
                    textoY,
                    8,
                    normal,
                    GRIS
            );

            textoY -= 12;
        }

        return y - alto - 12;
    }

    // =========================================================
    // NUEVA PÁGINA
    // =========================================================

    private PDPage nuevaPaginaConTitulo(
            PDDocument documento,
            String titulo,
            String subtitulo)
            throws IOException {

        PDPage page =
                new PDPage(A4);

        documento.addPage(page);

        try (PDPageContentStream contenido =
                     new PDPageContentStream(
                             documento,
                             page)) {

            dibujarFondo(contenido);

            dibujarEncabezado(
                    documento,
                    contenido,
                    titulo,
                    documento.getNumberOfPages(),
                    documento.getNumberOfPages()
            );

            escribirTexto(
                    contenido,
                    titulo,
                    MARGEN,
                    680,
                    18,
                    bold,
                    TEXTO
            );

            escribirTexto(
                    contenido,
                    subtitulo,
                    MARGEN,
                    659,
                    8,
                    normal,
                    GRIS
            );
        }

        return page;
    }

    // =========================================================
    // TABLAS
    // =========================================================

    private void dibujarTablaCabecera(
            PDPageContentStream contenido,
            float y,
            String[] encabezados,
            float[] anchos)
            throws IOException {

        float x = MARGEN;
        float alto = 28;

        float total = 0;

        for (float ancho : anchos) {
            total += ancho;
        }

        contenido.setNonStrokingColor(
                ROSA
        );

        contenido.addRect(
                x,
                y - alto,
                total,
                alto
        );

        contenido.fill();

        for (int i = 0;
                i < encabezados.length;
                i++) {

            escribirTexto(
                    contenido,
                    encabezados[i],
                    x + 5,
                    y - 18,
                    6.7f,
                    bold,
                    BLANCO
            );

            x += anchos[i];
        }
    }

    private void dibujarFila(
            PDPageContentStream contenido,
            float x,
            float y,
            String[] valores,
            float[] anchos)
            throws IOException {

        float alto = 35;

        float total = 0;

        for (float ancho : anchos) {
            total += ancho;
        }

        if ((int) (y / 35) % 2 == 0) {

            contenido.setNonStrokingColor(
                    new Color(252, 249, 250)
            );

            contenido.addRect(
                    x,
                    y - alto,
                    total,
                    alto
            );

            contenido.fill();
        }

        contenido.setStrokingColor(
                BORDE
        );

        contenido.setLineWidth(
                0.5f
        );

        contenido.moveTo(
                x,
                y - alto
        );

        contenido.lineTo(
                x + total,
                y - alto
        );

        contenido.stroke();

        float posicion = x;

        for (int i = 0;
                i < valores.length;
                i++) {

            escribirTexto(
                    contenido,
                    valores[i],
                    posicion + 5,
                    y - 20,
                    6.8f,
                    normal,
                    TEXTO
            );

            posicion += anchos[i];
        }
    }

    // =========================================================
    // KPI
    // =========================================================

    private void dibujarKpi(
            PDPageContentStream contenido,
            float x,
            float y,
            float ancho,
            float alto,
            String etiqueta,
            String valor)
            throws IOException {

        contenido.setNonStrokingColor(
                BLANCO
        );

        contenido.addRect(
                x,
                y,
                ancho,
                alto
        );

        contenido.fill();

        contenido.setStrokingColor(
                BORDE
        );

        contenido.addRect(
                x,
                y,
                ancho,
                alto
        );

        contenido.stroke();

        contenido.setNonStrokingColor(
                ROSA
        );

        contenido.addRect(
                x,
                y + alto - 4,
                ancho,
                4
        );

        contenido.fill();

        escribirTexto(
                contenido,
                etiqueta,
                x + 10,
                y + alto - 22,
                7,
                bold,
                GRIS
        );

        escribirTexto(
                contenido,
                valor,
                x + 10,
                y + 27,
                12,
                bold,
                TEXTO
        );
    }

    // =========================================================
    // ENCABEZADO
    // =========================================================

    private void dibujarEncabezado(
            PDDocument documento,
            PDPageContentStream contenido,
            String titulo,
            int pagina,
            int totalPaginas)
            throws IOException {

        contenido.setNonStrokingColor(
                BLANCO
        );

        contenido.addRect(
                0,
                ALTO - 94,
                ANCHO,
                94
        );

        contenido.fill();

        contenido.setNonStrokingColor(
                ROSA
        );

        contenido.addRect(
                0,
                ALTO - 5,
                ANCHO,
                5
        );

        contenido.fill();

        cargarLogo(
                documento,
                contenido
        );

        escribirTexto(
                contenido,
                "BACKHOME",
                88,
                ALTO - 39,
                19,
                bold,
                TEXTO
        );

        escribirTexto(
                contenido,
                "Sistema de gestión y adopción responsable",
                88,
                ALTO - 54,
                7.5f,
                normal,
                GRIS
        );

        escribirTexto(
                contenido,
                titulo,
                MARGEN,
                ALTO - 82,
                8,
                bold,
                ROSA_OSCURO
        );

        escribirTextoDerecha(
                contenido,
                "Generado: "
                        + LocalDateTime.now()
                        .format(fechaHora),
                ANCHO - MARGEN,
                ALTO - 38,
                7.5f,
                normal,
                GRIS
        );
    }

    // =========================================================
    // LOGO
    // =========================================================

    private void cargarLogo(
            PDDocument documento,
            PDPageContentStream contenido)
            throws IOException {

        String[] rutas = {
                "/static/images/logo-backhome.png",
                "/images/logo-backhome.png"
        };

        for (String ruta : rutas) {

            try (InputStream input =
                         getClass()
                                 .getResourceAsStream(ruta)) {

                if (input != null) {

                    PDImageXObject logo =
                            PDImageXObject.createFromByteArray(
                                    documento,
                                    input.readAllBytes(),
                                    "logo-backhome"
                            );

                    contenido.drawImage(
                            logo,
                            MARGEN,
                            ALTO - 66,
                            38,
                            38
                    );

                    return;
                }
            }
        }
    }

    // =========================================================
    // PIE
    // =========================================================

    private void dibujarPiePagina(
            PDPageContentStream contenido,
            int pagina,
            int totalPaginas)
            throws IOException {

        contenido.setStrokingColor(
                BORDE
        );

        contenido.moveTo(
                MARGEN,
                41
        );

        contenido.lineTo(
                ANCHO - MARGEN,
                41
        );

        contenido.stroke();

        escribirTexto(
                contenido,
                "BackHome | Reporte administrativo",
                MARGEN,
                28,
                7,
                normal,
                GRIS_CLARO
        );
    }

    private void dibujarNumeroPagina(
            PDPageContentStream contenido,
            int pagina,
            int totalPaginas)
            throws IOException {

        escribirTextoDerecha(
                contenido,
                "Página "
                        + pagina
                        + " de "
                        + totalPaginas,
                ANCHO - MARGEN,
                28,
                7,
                normal,
                GRIS_CLARO
        );
    }

    // =========================================================
    // FONDO
    // =========================================================

    private void dibujarFondo(
            PDPageContentStream contenido)
            throws IOException {

        contenido.setNonStrokingColor(
                FONDO
        );

        contenido.addRect(
                0,
                0,
                ANCHO,
                ALTO
        );

        contenido.fill();
    }

    // =========================================================
    // TEXTO
    // =========================================================

    private void escribirTexto(
            PDPageContentStream contenido,
            String texto,
            float x,
            float y,
            float tamaño,
            PDType1Font fuente,
            Color color)
            throws IOException {

        if (texto == null) {
            texto = "";
        }

        contenido.beginText();

        contenido.setFont(
                fuente,
                tamaño
        );

        contenido.setNonStrokingColor(
                color
        );

        contenido.newLineAtOffset(
                x,
                y
        );

        contenido.showText(
                limpiarTexto(texto)
        );

        contenido.endText();
    }

    private void escribirTextoDerecha(
            PDPageContentStream contenido,
            String texto,
            float xDerecha,
            float y,
            float tamaño,
            PDType1Font fuente,
            Color color)
            throws IOException {

        String limpio =
                limpiarTexto(texto);

        float ancho =
                fuente.getStringWidth(
                        limpio
                ) / 1000f * tamaño;

        escribirTexto(
                contenido,
                limpio,
                xDerecha - ancho,
                y,
                tamaño,
                fuente,
                color
        );
    }

    // =========================================================
    // UTILIDADES
    // =========================================================

    private String nombreTipo(
            String tipo) {

        switch (tipo) {

            case "usuarios":
                return "Usuarios";

            case "refugios":
                return "Refugios";

            case "animales":
                return "Animales";

            case "seguimientos":
                return "Seguimientos";

            case "donaciones":
                return "Donaciones";

            case "consejos":
                return "Consejos";

            default:
                return tipo;
        }
    }

    private String formatoDinero(
            BigDecimal monto) {

        if (monto == null) {
            monto = BigDecimal.ZERO;
        }

        return "$ "
                + monto.setScale(
                        2,
                        RoundingMode.HALF_UP
                ).toPlainString();
    }

    private String cortar(
            String texto,
            int maximo) {

        if (texto == null
                || texto.isBlank()) {

            return "-";
        }

        if (texto.length() <= maximo) {
            return texto;
        }

        return texto.substring(
                0,
                Math.max(1, maximo - 3)
        ) + "...";
    }

    private String limpiarTexto(
            String texto) {

        if (texto == null) {
            return "";
        }

        return texto
                .replace("á", "a")
                .replace("é", "e")
                .replace("í", "i")
                .replace("ó", "o")
                .replace("ú", "u")
                .replace("Á", "A")
                .replace("É", "E")
                .replace("Í", "I")
                .replace("Ó", "O")
                .replace("Ú", "U")
                .replace("ñ", "n")
                .replace("Ñ", "N")
                .replace("ü", "u")
                .replace("Ü", "U")
                .replace("¿", "?")
                .replace("¡", "!")
                .replace("–", "-")
                .replace("—", "-")
                .replace("“", "\"")
                .replace("”", "\"")
                .replace("‘", "'")
                .replace("’", "'")
                .replaceAll(
                        "[^\\x20-\\x7E]",
                        ""
                );
    }

    private List<String> dividirTexto(
            String texto,
            int maxCaracteres) {

        List<String> resultado =
                new ArrayList<>();

        if (texto == null
                || texto.isBlank()) {

            resultado.add("");

            return resultado;
        }

        String[] palabras =
                limpiarTexto(texto)
                        .split("\\s+");

        StringBuilder actual =
                new StringBuilder();

        for (String palabra : palabras) {

            if (actual.length() == 0) {

                actual.append(palabra);

            } else if (
                    actual.length()
                            + palabra.length()
                            + 1
                            <= maxCaracteres) {

                actual.append(" ")
                        .append(palabra);

            } else {

                resultado.add(
                        actual.toString()
                );

                actual.setLength(0);

                actual.append(palabra);
            }
        }

        if (actual.length() > 0) {
            resultado.add(
                    actual.toString()
            );
        }

        return resultado;
    }

    private ResponseEntity<byte[]> respuestaPdf(
            byte[] pdf,
            String nombreArchivo) {

        HttpHeaders headers =
                new HttpHeaders();

        headers.setContentType(
                MediaType.APPLICATION_PDF
        );

        headers.setContentDisposition(
                ContentDisposition.attachment()
                        .filename(nombreArchivo)
                        .build()
        );

        headers.setContentLength(
                pdf.length
        );

        return ResponseEntity
                .ok()
                .headers(headers)
                .body(pdf);
    }
}