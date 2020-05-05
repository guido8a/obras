package janus

import com.lowagie.text.*
import com.lowagie.text.pdf.PdfContentByte
import com.lowagie.text.pdf.PdfPCell
import com.lowagie.text.pdf.PdfPTable
import com.lowagie.text.pdf.PdfWriter
import janus.pac.CronogramaContratado
import janus.seguridad.Shield
import jxl.Workbook
import jxl.WorkbookSettings
import jxl.write.*

import java.awt.*

class ReportesRubrosController extends Shield {

    def preciosService
    def reportesPdfService

    def index() { }

    private static void addEmptyLine(Paragraph paragraph, int number) {
        for (int i = 0; i < number; i++) {
            paragraph.add(new Paragraph(" "));
        }
    }

    private static int[] arregloEnteros(array) {
        int[] ia = new int[array.size()]
        array.eachWithIndex { it, i ->
            ia[i] = it.toInteger()
        }
        return ia
    }

    private String numero(num, decimales, cero) {
        if (num == 0 && cero.toString().toLowerCase() == "hide") {
            return " ";
        }
        if (decimales == 0) {
            return formatNumber(number: num, minFractionDigits: decimales, maxFractionDigits: decimales, locale: "ec")
        } else {
            def format
            if (decimales == 2) {
                format = "##,##0"
            } else if (decimales == 3) {
                format = "##,###0"
            }
            return formatNumber(number: num, minFractionDigits: decimales, maxFractionDigits: decimales, locale: "ec", format: format)
        }
    }


    private String numero(num, decimales) {
        return numero(num, decimales, "show")
    }

    private String numero(num) {
        return numero(num, 3)
    }



    def reporteRubrosTransporteV2(){
        println("params " + params)
        def auxiliar = Auxiliar.get(1)

        def obra
        def fecha
        def fecha1
        def rubro = Item.get(params.id)

        if(params.fecha){
            fecha = new Date().parse("dd-MM-yyyy", params.fecha)
        }

        if(params.fechaSalida){
            fecha1 = new Date().parse("dd-MM-yyyy", params.fechaSalida)
        }

        def bandMat = 0
        def band = 0
        def bandTrans = params.trans
        def lugar = params.lugar
        def indi = params.indi
        def listas = params.listas
        def total = 0, totalHer = 0, totalMan = 0, totalMat = 0, totalHerRel = 0, totalHerVae = 0, totalManRel = 0,
            totalManVae = 0, totalMatRel = 0, totalMatVae = 0, totalTRel=0, totalTVae=0

        try {
            indi = indi.toDouble()
        } catch (e) {
            println "error parse " + e
            indi = 21.5
        }

        if (params.obra) {
            obra = Obra.get(params.obra)
        }

        def parametros = "" + rubro.id + ",'" + fecha.format("yyyy-MM-dd") + "'," + listas + "," + params.dsp0 + "," +
                params.dsp1 + "," + params.dsv0 + "," + params.dsv1 + "," + params.dsv2 + "," + params.chof + "," + params.volq

        preciosService.ac_rbroV2(params.id, fecha.format("yyyy-MM-dd"), params.lugar)
        def res = preciosService.rb_preciosAsc(parametros, "")
//        def vae = preciosService.rb_preciosVae(parametros, "")

        def prmsHeaderHoja = [border: Color.WHITE]
        def prmsFila = [border: Color.WHITE, align: Element.ALIGN_CENTER, valign: Element.ALIGN_MIDDLE]
        def prmsFilaIzquierda = [border: Color.WHITE, align : Element.ALIGN_LEFT, valign: Element.ALIGN_LEFT]
        def prmsFilaDerecha = [border: Color.WHITE, align : Element.ALIGN_RIGHT, valign: Element.ALIGN_RIGHT]
        def prmsHeaderHoja2 = [border: Color.WHITE, colspan: 9]
        def prmsHeader = [border: Color.WHITE, colspan: 7, bg: new Color(73, 175, 205),
                          align : Element.ALIGN_CENTER, valign: Element.ALIGN_MIDDLE]
        def prmsHeader2 = [border: Color.WHITE, colspan: 3, bg: new Color(73, 175, 205),
                           align : Element.ALIGN_CENTER, valign: Element.ALIGN_MIDDLE]
        def prmsCellHead = [border: Color.WHITE, bg: new Color(73, 175, 205),
                            align : Element.ALIGN_CENTER, valign: Element.ALIGN_MIDDLE]
        def prmsCellCenter = [border: Color.BLACK, align: Element.ALIGN_CENTER, valign: Element.ALIGN_MIDDLE]
        def prmsCellRight = [border: Color.BLACK, align: Element.ALIGN_RIGHT, valign: Element.ALIGN_RIGHT]
        def prmsCellLeft = [border: Color.BLACK, valign: Element.ALIGN_MIDDLE]
        def prmsSubtotal = [border: Color.BLACK, colspan: 6,
                            align : Element.ALIGN_RIGHT, valign: Element.ALIGN_MIDDLE]
        def prmsNum = [border: Color.BLACK, align: Element.ALIGN_RIGHT, valign: Element.ALIGN_MIDDLE]

        def celdaCabecera = [border: Color.BLACK, bg: new Color(220, 220, 220), align: Element.ALIGN_CENTER, valign: Element.ALIGN_MIDDLE, bordeBot: "1"]
//        def celdaCabecera = [bg: new Color(220, 220, 220), align: Element.ALIGN_CENTER, valign: Element.ALIGN_MIDDLE, bordeBot: "1"]

        def celdaCabeceraIzquierda = [bct: Color.BLACK, bcl: Color.WHITE, bcr:Color.WHITE, bcb: Color.WHITE, align: Element.ALIGN_LEFT, valign: Element.ALIGN_LEFT]
        def celdaCabeceraDerecha = [bct: Color.BLACK, bcl: Color.WHITE, bcr:Color.WHITE, bcb: Color.WHITE, align: Element.ALIGN_RIGHT, valign: Element.ALIGN_RIGHT]
        def celdaCabeceraCentro = [bct: Color.BLACK, bcl: Color.WHITE, bcr:Color.WHITE, bcb: Color.WHITE, align: Element.ALIGN_CENTER, valign: Element.ALIGN_CENTER]
        def celdaCabeceraCentro2 = [bcb: Color.BLACK, bcl: Color.WHITE, bcr:Color.WHITE, bct: Color.WHITE, align: Element.ALIGN_CENTER, valign: Element.ALIGN_CENTER]
        def celdaCabeceraDerecha2 = [bcb: Color.BLACK, bcl: Color.WHITE, bcr:Color.WHITE, bct: Color.WHITE, align: Element.ALIGN_RIGHT, valign: Element.ALIGN_RIGHT]
        def celdaCabeceraIzquierda2 = [bcb: Color.BLACK, bcl: Color.WHITE, bcr:Color.WHITE, bct: Color.WHITE, align: Element.ALIGN_LEFT, valign: Element.ALIGN_LEFT]

        def tituloRubro = [height: 25, border: Color.WHITE, colspan: 12, align : Element.ALIGN_LEFT, valign: Element.ALIGN_MIDDLE]


        def prms = [prmsHeaderHoja: prmsHeaderHoja, prmsHeader: prmsHeader, prmsHeader2: prmsHeader2,
                    prmsCellHead: prmsCellHead, prmsCell: prmsCellCenter, prmsCellLeft: prmsCellLeft, prmsSubtotal: prmsSubtotal, prmsNum: prmsNum, prmsHeaderHoja2: prmsHeaderHoja2, prmsCellRight: prmsCellRight]

        Font times12bold = new Font(Font.TIMES_ROMAN, 12, Font.BOLD);
        Font times14bold = new Font(Font.TIMES_ROMAN, 14, Font.BOLD);
        Font times10bold = new Font(Font.TIMES_ROMAN, 10, Font.BOLD);
        Font times10normal = new Font(Font.TIMES_ROMAN, 10, Font.NORMAL);
        Font times8bold = new Font(Font.TIMES_ROMAN, 8, Font.BOLD)
        Font times8normal = new Font(Font.TIMES_ROMAN, 8, Font.NORMAL)
        Font times7bold = new Font(Font.TIMES_ROMAN, 7, Font.BOLD)
        Font times7normal = new Font(Font.TIMES_ROMAN, 7, Font.NORMAL)
        Font times10boldWhite = new Font(Font.TIMES_ROMAN, 10, Font.BOLD);
        Font times8boldWhite = new Font(Font.TIMES_ROMAN, 8, Font.BOLD)
        times8boldWhite.setColor(Color.WHITE)
        times10boldWhite.setColor(Color.WHITE)

        def baos = new ByteArrayOutputStream()
        def name = "reporteRubrosTransporte_" + new Date().format("ddMMyyyy_hhmm") + ".pdf";

        Document document
        document = new Document(PageSize.A4)
//        document.setMargins(marginLeft, marginRight, marginTop, marginBottom) 1/72 de pulgada, 1cm = 28.3
        document.setMargins(60, 24, 45, 45);

        def pdfw = PdfWriter.getInstance(document, baos);
        document.open();
        document.addTitle("Rubros " + new Date().format("dd_MM_yyyy"));
        document.addSubject("Generado por el sistema Obras");
        document.addKeywords("documentosObra, janus, rubros");
        document.addAuthor("OBRAS");
        document.addCreator("Tedein SA");

        Paragraph headers = new Paragraph();
        addEmptyLine(headers, 1);
        headers.setAlignment(Element.ALIGN_CENTER);
        headers.add(new Paragraph("G.A.D. LOS RÍOS", times14bold));
        headers.add(new Paragraph("COORDINACIÓN DE FIJACIÓN DE PRECIOS UNITARIOS - ANÁLISIS DE PRECIOS UNITARIOS", times10bold));
        headers.add(new Paragraph("", times14bold));
        document.add(headers)

        PdfPTable tablaCoeficiente = new PdfPTable(6);
        tablaCoeficiente.setWidthPercentage(100);
        tablaCoeficiente.setWidths(arregloEnteros([18,20, 18,23, 20,15]))

        reportesPdfService.addCellTb(tablaCoeficiente, new Paragraph("Código de rubro: ", times10bold), prmsHeaderHoja)
        reportesPdfService.addCellTb(tablaCoeficiente, new Paragraph((rubro?.codigo ?: ''), times10normal), prmsHeaderHoja)
        reportesPdfService.addCellTb(tablaCoeficiente, new Paragraph("Fecha: ", times10bold), [border: Color.WHITE, align: Element.ALIGN_RIGHT])
        reportesPdfService.addCellTb(tablaCoeficiente, new Paragraph((fecha1?.format("dd-MM-yyyy") ?: ''), times10normal), [border: Color.WHITE, align: Element.ALIGN_LEFT])
        reportesPdfService.addCellTb(tablaCoeficiente, new Paragraph("Fecha Act. P.U: ", times10bold), prmsHeaderHoja)
        reportesPdfService.addCellTb(tablaCoeficiente, new Paragraph((fecha?.format("dd-MM-yyyy") ?: '') , times10normal), prmsHeaderHoja)

        reportesPdfService.addCellTb(tablaCoeficiente, new Paragraph("Especificación: ", times10bold), prmsHeaderHoja)
        reportesPdfService.addCellTb(tablaCoeficiente, new Paragraph((rubro?.codigoEspecificacion ?: ''), times10normal), [border: Color.WHITE, colspan: 3])
        reportesPdfService.addCellTb(tablaCoeficiente, new Paragraph("Unidad: ", times10bold), prmsHeaderHoja)
        reportesPdfService.addCellTb(tablaCoeficiente, new Paragraph((rubro?.unidad?.codigo ?: ''), times10normal), prmsHeaderHoja)

        reportesPdfService.addCellTb(tablaCoeficiente, new Paragraph("Descripción: ", times10bold), prmsHeaderHoja)
        reportesPdfService.addCellTb(tablaCoeficiente, new Paragraph((rubro?.nombre ?: ''), times10normal), [border: Color.WHITE, colspan: 5])


        //EQUIPOS
        PdfPTable tablaEquipos = new PdfPTable(7);
        tablaEquipos.setWidthPercentage(100);
        tablaEquipos.setWidths(arregloEnteros([8,40,8,9,8,10,8]))

//        reportesPdfService.addCellTb(tablaEquipos, new Paragraph("EQUIPOS", times14bold), [border: Color.WHITE, colspan: 7, align : Element.ALIGN_CENTER, valign: Element.ALIGN_MIDDLE])
        reportesPdfService.addCellTb(tablaEquipos, new Paragraph("EQUIPOS", times12bold), tituloRubro)

        reportesPdfService.addCellTb(tablaEquipos, new Paragraph("CÓDIGO", times7bold), celdaCabecera)
        reportesPdfService.addCellTb(tablaEquipos, new Paragraph("DESCRIPCIÓN", times7bold), celdaCabecera)
        reportesPdfService.addCellTb(tablaEquipos, new Paragraph("CANTIDAD", times7bold), celdaCabecera)
        reportesPdfService.addCellTb(tablaEquipos, new Paragraph("TARIFA(\$/H)", times7bold), celdaCabecera)
        reportesPdfService.addCellTb(tablaEquipos, new Paragraph("COSTOS(\$)", times7bold), celdaCabecera)
        reportesPdfService.addCellTb(tablaEquipos, new Paragraph("RENDIMIENTO", times7bold), celdaCabecera)
        reportesPdfService.addCellTb(tablaEquipos, new Paragraph("C.TOTAL(\$)", times7bold), celdaCabecera)

        res.eachWithIndex { r, i ->
            if (r["grpocdgo"] == 3) {
                reportesPdfService.addCellTb(tablaEquipos, new Paragraph(r["itemcdgo"], times8normal), prmsFilaIzquierda)
                reportesPdfService.addCellTb(tablaEquipos, new Paragraph(r["itemnmbr"], times8normal), prmsFilaIzquierda)
                reportesPdfService.addCellTb(tablaEquipos, new Paragraph(numero(r["rbrocntd"], 5)?.toString(), times8normal), prmsFilaDerecha)
                reportesPdfService.addCellTb(tablaEquipos, new Paragraph(numero(r["rbpcpcun"], 5)?.toString(), times8normal), prmsFilaDerecha)
                reportesPdfService.addCellTb(tablaEquipos, new Paragraph((numero((r["rbpcpcun"] * r["rbrocntd"]), 5))?.toString(), times8normal), prmsFilaDerecha)
                reportesPdfService.addCellTb(tablaEquipos, new Paragraph(numero(r["rndm"], 5)?.toString(), times8normal), prmsFilaDerecha)
                reportesPdfService.addCellTb(tablaEquipos, new Paragraph(numero(r["parcial"], 5)?.toString(), times8normal), prmsFilaDerecha)
                totalHer += r["parcial"]
            }
        }

        reportesPdfService.addCellTb(tablaEquipos, new Paragraph("", times14bold), [border: Color.WHITE, colspan: 5])
        reportesPdfService.addCellTb(tablaEquipos, new Paragraph("TOTAL", times8bold), prmsFilaDerecha)
        reportesPdfService.addCellTb(tablaEquipos, new Paragraph(numero(totalHer, 5)?.toString(), times8bold), prmsFilaDerecha)
        reportesPdfService.addCellTb(tablaEquipos, new Paragraph("", times14bold), [border: Color.WHITE, colspan: 7])

        //MANO DE OBRA
        PdfPTable tablaManoObra = new PdfPTable(7);
        tablaManoObra.setWidthPercentage(100);
        tablaManoObra.setWidths(arregloEnteros([6,42,8,9,8,10,8]))

        reportesPdfService.addCellTb(tablaManoObra, new Paragraph("MANO DE OBRA", times12bold), tituloRubro)

        reportesPdfService.addCellTb(tablaManoObra, new Paragraph("CÓDIGO", times7bold), celdaCabecera)
        reportesPdfService.addCellTb(tablaManoObra, new Paragraph("DESCRIPCIÓN", times7bold), celdaCabecera)
        reportesPdfService.addCellTb(tablaManoObra, new Paragraph("CANTIDAD", times7bold), celdaCabecera)
        reportesPdfService.addCellTb(tablaManoObra, new Paragraph("JORNAL(\$/H)", times7bold), celdaCabecera)
        reportesPdfService.addCellTb(tablaManoObra, new Paragraph("COSTOS(\$)", times7bold), celdaCabecera)
        reportesPdfService.addCellTb(tablaManoObra, new Paragraph("RENDIMIENTO", times7bold), celdaCabecera)
        reportesPdfService.addCellTb(tablaManoObra, new Paragraph("C.TOTAL(\$)", times7bold), celdaCabecera)

        res.eachWithIndex { r, i ->
            if (r["grpocdgo"] == 2) {
                reportesPdfService.addCellTb(tablaManoObra, new Paragraph(r["itemcdgo"], times8normal), prmsFilaIzquierda)
                reportesPdfService.addCellTb(tablaManoObra, new Paragraph(r["itemnmbr"], times8normal), prmsFilaIzquierda)
                reportesPdfService.addCellTb(tablaManoObra, new Paragraph(numero(r["rbrocntd"], 5)?.toString(), times8normal), prmsFilaDerecha)
                reportesPdfService.addCellTb(tablaManoObra, new Paragraph(numero(r["rbpcpcun"], 5)?.toString(), times8normal), prmsFilaDerecha)
                reportesPdfService.addCellTb(tablaManoObra, new Paragraph((numero((r["rbpcpcun"] * r["rbrocntd"]), 5))?.toString(), times8normal), prmsFilaDerecha)
                reportesPdfService.addCellTb(tablaManoObra, new Paragraph(numero(r["rndm"], 5)?.toString(), times8normal), prmsFilaDerecha)
                reportesPdfService.addCellTb(tablaManoObra, new Paragraph(numero(r["parcial"], 5)?.toString(), times8normal), prmsFilaDerecha)
                totalMan += r["parcial"]
            }
        }

        reportesPdfService.addCellTb(tablaManoObra, new Paragraph("", times8bold), [border: Color.WHITE, colspan: 5])
        reportesPdfService.addCellTb(tablaManoObra, new Paragraph("TOTAL", times8bold), prmsFilaDerecha)
        reportesPdfService.addCellTb(tablaManoObra, new Paragraph(numero(totalMan, 5)?.toString(), times8bold), prmsFilaDerecha)
        reportesPdfService.addCellTb(tablaManoObra, new Paragraph("", times8bold), [border: Color.WHITE, colspan: 7])

        //MATERIALES
        PdfPTable tablaMateriales = new PdfPTable(6);
        tablaMateriales.setWidthPercentage(100);
        tablaMateriales.setWidths(arregloEnteros([8,48,9,8,10,8]))

        if(params.trans == 'no'){
            reportesPdfService.addCellTb(tablaMateriales, new Paragraph("MATERIALES INCLUIDO TRANSPORTE", times12bold), tituloRubro)
        }else{
            reportesPdfService.addCellTb(tablaMateriales, new Paragraph("MATERIALES", times12bold), tituloRubro)
        }

        reportesPdfService.addCellTb(tablaMateriales, new Paragraph("CÓDIGO", times7bold), celdaCabecera)
        reportesPdfService.addCellTb(tablaMateriales, new Paragraph("DESCRIPCIÓN", times7bold), celdaCabecera)
        reportesPdfService.addCellTb(tablaMateriales, new Paragraph("UNIDAD", times7bold), celdaCabecera)
        reportesPdfService.addCellTb(tablaMateriales, new Paragraph("CANTIDAD", times7bold), celdaCabecera)
        reportesPdfService.addCellTb(tablaMateriales, new Paragraph("UNITARIO(\$)", times7bold), celdaCabecera)
        reportesPdfService.addCellTb(tablaMateriales, new Paragraph("C.TOTAL(\$)", times7bold), celdaCabecera)

        res.eachWithIndex { r, i ->
            if (r["grpocdgo"] == 1) {
                bandMat = 1
                reportesPdfService.addCellTb(tablaMateriales, new Paragraph(r["itemcdgo"], times8normal), prmsFilaIzquierda)
                reportesPdfService.addCellTb(tablaMateriales, new Paragraph(r["itemnmbr"], times8normal), prmsFilaIzquierda)
                reportesPdfService.addCellTb(tablaMateriales, new Paragraph(r["unddcdgo"], times8normal), prmsFila)
                reportesPdfService.addCellTb(tablaMateriales, new Paragraph(numero(r["rbrocntd"], 5)?.toString(), times8normal), prmsFila)
                if (params.trans != 'no') {
                    reportesPdfService.addCellTb(tablaMateriales, new Paragraph(numero(r["rbpcpcun"], 5)?.toString(), times8normal), prmsFilaDerecha)
                    reportesPdfService.addCellTb(tablaMateriales, new Paragraph(numero(r["parcial"], 5)?.toString(), times8normal), prmsFilaDerecha)
                    totalMat += r["parcial"]
                }else{
                    reportesPdfService.addCellTb(tablaMateriales, new Paragraph(numero((r["rbpcpcun"] + r["parcial_t"] / r["rbrocntd"]), 5)?.toString(), times8normal), prmsFilaDerecha)
                    reportesPdfService.addCellTb(tablaMateriales, new Paragraph(numero((r["parcial"] + r["parcial_t"]), 5)?.toString(), times8normal), prmsFilaDerecha)
                    totalMat += (r["parcial"] + r["parcial_t"])
                }
            }
        }

        reportesPdfService.addCellTb(tablaMateriales, new Paragraph("", times8bold), [border: Color.WHITE, colspan: 4])
        reportesPdfService.addCellTb(tablaMateriales, new Paragraph("TOTAL", times8bold), prmsFilaDerecha)
        reportesPdfService.addCellTb(tablaMateriales, new Paragraph(numero(totalMat, 5)?.toString(), times8bold), prmsFilaDerecha)

        reportesPdfService.addCellTb(tablaMateriales, new Paragraph("", times8bold), [border: Color.WHITE, colspan: 6])

        //TRANSPORTE
        PdfPTable tablaTransporte = new PdfPTable(8);
        tablaTransporte.setWidthPercentage(100);
        tablaTransporte.setWidths(arregloEnteros([11,25,8,11,11,12,10,10]))

        reportesPdfService.addCellTb(tablaTransporte, new Paragraph("TRANSPORTE", times12bold), tituloRubro)

        reportesPdfService.addCellTb(tablaTransporte, new Paragraph("CÓDIGO", times7bold), celdaCabecera)
        reportesPdfService.addCellTb(tablaTransporte, new Paragraph("DESCRIPCIÓN", times7bold), celdaCabecera)
        reportesPdfService.addCellTb(tablaTransporte, new Paragraph("UNIDAD", times7bold), celdaCabecera)
        reportesPdfService.addCellTb(tablaTransporte, new Paragraph("PES/VOL", times7bold), celdaCabecera)
        reportesPdfService.addCellTb(tablaTransporte, new Paragraph("CANTIDAD", times7bold), celdaCabecera)
        reportesPdfService.addCellTb(tablaTransporte, new Paragraph("DISTANCIA", times7bold), celdaCabecera)
        reportesPdfService.addCellTb(tablaTransporte, new Paragraph("TARIFA", times7bold), celdaCabecera)
        reportesPdfService.addCellTb(tablaTransporte, new Paragraph("C.TOTAL(\$)", times7bold), celdaCabecera)

        res.eachWithIndex { r, i ->
            if (r["grpocdgo"]== 1 && params.trans != 'no') {
                reportesPdfService.addCellTb(tablaTransporte, new Paragraph(r["itemcdgo"], times8normal), prmsFilaIzquierda)
                reportesPdfService.addCellTb(tablaTransporte, new Paragraph(r["itemnmbr"], times8normal), prmsFilaIzquierda)
                if(r["tplscdgo"].trim() =='P' || r["tplscdgo"].trim() =='P1' ){
                    reportesPdfService.addCellTb(tablaTransporte, new Paragraph("ton-km", times8normal), prmsFila)
                }else{
                    if(r["tplscdgo"].trim() =='V' || r["tplscdgo"].trim() =='V1' || r["tplscdgo"].trim() =='V2') {
                        reportesPdfService.addCellTb(tablaTransporte, new Paragraph("m3-km", times8normal), prmsFila)
                    }else{
                        reportesPdfService.addCellTb(tablaTransporte, new Paragraph(r["unddcdgo"], times8normal), prmsFila)
                    }
                }
                reportesPdfService.addCellTb(tablaTransporte, new Paragraph(numero(r["itempeso"], 5)?.toString(), times8normal), prmsFila)
                reportesPdfService.addCellTb(tablaTransporte, new Paragraph(numero(r["rbrocntd"], 5)?.toString(), times8normal), prmsFila)
                reportesPdfService.addCellTb(tablaTransporte, new Paragraph(numero(r["distancia"], 5)?.toString(), times8normal), prmsFila)
                reportesPdfService.addCellTb(tablaTransporte, new Paragraph(numero(r["tarifa"], 5)?.toString(), times8normal), prmsFila)
                reportesPdfService.addCellTb(tablaTransporte, new Paragraph(numero(r["parcial_t"], 5)?.toString(), times8normal), prmsFilaDerecha)
                total += r["parcial_t"]
            }
        }

        reportesPdfService.addCellTb(tablaTransporte, new Paragraph("", times8bold), [border: Color.WHITE, colspan: 6])
        reportesPdfService.addCellTb(tablaTransporte, new Paragraph("TOTAL", times8bold), prmsFila)
        reportesPdfService.addCellTb(tablaTransporte, new Paragraph(numero(total, 5)?.toString(), times8bold), prmsFilaDerecha)

        reportesPdfService.addCellTb(tablaTransporte, new Paragraph("", times8bold), [border: Color.WHITE, colspan: 6])
        reportesPdfService.addCellTb(tablaTransporte, new Paragraph("", times8bold), [border: Color.WHITE, colspan: 6])


        //COSTOS INDIRECTOS
        def totalRubro

        if (!params.trans) {
            totalRubro = total + totalHer + totalMan + totalMat
        } else {
            totalRubro = totalHer + totalMan + totalMat
        }

        def totalIndi = totalRubro?.toDouble() * indi / 100

        PdfPTable tablaIndirectos = new PdfPTable(3);
        tablaIndirectos.setWidthPercentage(70);
        tablaIndirectos.setWidths(arregloEnteros([50,25,25]))
        tablaIndirectos.horizontalAlignment = Element.ALIGN_LEFT;

        reportesPdfService.addCellTb(tablaIndirectos, new Paragraph("COSTOS INDIRECTOS", times12bold), tituloRubro)

        reportesPdfService.addCellTb(tablaIndirectos, new Paragraph("DESCRIPCIÓN", times7bold), celdaCabecera)
        reportesPdfService.addCellTb(tablaIndirectos, new Paragraph("PORCENTAJE", times7bold), celdaCabecera)
        reportesPdfService.addCellTb(tablaIndirectos, new Paragraph("VALOR", times7bold), celdaCabecera)

        reportesPdfService.addCellTb(tablaIndirectos, new Paragraph("COSTOS INDIRECTOS", times8normal), prmsFilaIzquierda)
        reportesPdfService.addCellTb(tablaIndirectos, new Paragraph(numero(indi, 1)?.toString() + "%", times8normal), prmsFila)
        reportesPdfService.addCellTb(tablaIndirectos, new Paragraph(numero(totalIndi, 5)?.toString(), times8normal), prmsFila)

        reportesPdfService.addCellTb(tablaIndirectos, new Paragraph("", times8bold), [border: Color.WHITE, colspan: 3])


        PdfPTable tablaTotales = new PdfPTable(2);
        tablaTotales.setWidthPercentage(40);
        tablaTotales.setWidths(arregloEnteros([50,25]))
        tablaTotales.horizontalAlignment = Element.ALIGN_RIGHT;

        reportesPdfService.addCellTb(tablaTotales, new Paragraph("COSTO UNITARIO DIRECTO", times8bold), celdaCabeceraIzquierda)
        reportesPdfService.addCellTb(tablaTotales, new Paragraph(numero(totalRubro, 2)?.toString(), times8bold), celdaCabeceraDerecha)

        reportesPdfService.addCellTb(tablaTotales, new Paragraph("COSTOS INDIRECTO", times8bold), prmsFilaIzquierda)
        reportesPdfService.addCellTb(tablaTotales, new Paragraph(numero(totalIndi, 2)?.toString(), times8bold), prmsFilaDerecha)

        reportesPdfService.addCellTb(tablaTotales, new Paragraph("COSTO TOTAL DEL RUBRO", times8bold), prmsFilaIzquierda)
        reportesPdfService.addCellTb(tablaTotales, new Paragraph(numero((totalRubro + totalIndi), 2)?.toString(), times8bold), prmsFilaDerecha)

        reportesPdfService.addCellTb(tablaTotales, new Paragraph("PRECIO UNITARIO \$USD", times8bold), celdaCabeceraIzquierda2)
        reportesPdfService.addCellTb(tablaTotales, new Paragraph(numero((totalRubro + totalIndi), 2)?.toString(), times8bold), celdaCabeceraDerecha2)

        document.add(tablaCoeficiente)
        document.add(tablaEquipos)
        document.add(tablaManoObra)
        document.add(tablaMateriales)

        if (total == 0 || params.trans == "no"){
        }else{
            document.add(tablaTransporte)
        }

        document.add(tablaIndirectos)
        document.add(tablaTotales)

        PdfPTable tablaNota = new PdfPTable(2);
        tablaNota.setWidthPercentage(100);
        tablaNota.setWidths(arregloEnteros([6, 94]))
        reportesPdfService.addCellTb(tablaNota, new Paragraph("Nota:", times8bold), prmsFilaIzquierda)
        reportesPdfService.addCellTb(tablaNota, new Paragraph("Los cálculos se hacen con todos los " +
                "decimales y el resultado final se lo redondea a dos decimales.", times8normal), prmsFilaIzquierda)
        document.add(tablaNota)

        document.close();
        pdfw.close()
        byte[] b = baos.toByteArray();
        response.setContentType("application/pdf")
        response.setHeader("Content-disposition", "attachment; filename=" + name)
        response.setContentLength(b.length)
        response.getOutputStream().write(b)
    }


    def reporteRubrosV2() {

        println("params " + params)

        def auxiliar = Auxiliar.get(1)

        def obra
        def fecha
        def fecha1
        def rubro = Item.get(params.id)

        if(params.fecha){
            fecha = new Date().parse("dd-MM-yyyy", params.fecha)
        }

        if(params.fechaSalida){
            fecha1 = new Date().parse("dd-MM-yyyy", params.fechaSalida)
        }

        def bandMat = 0
        def band = 0
        def bandTrans = params.trans
        def lugar = params.lugar
        def indi = params.indi
        def listas = params.listas
        def total = 0, totalHer = 0, totalMan = 0, totalMat = 0, totalHerRel = 0, totalHerVae = 0, totalManRel = 0,
            totalManVae = 0, totalMatRel = 0, totalMatVae = 0, totalTRel=0, totalTVae=0

        try {
            indi = indi.toDouble()
        } catch (e) {
            println "error parse " + e
            indi = 22
        }

        if (params.obra) {
            obra = Obra.get(params.obra)
        }

        def parametros = "" + rubro.id + ",'" + fecha.format("yyyy-MM-dd") + "'," + listas + "," + params.dsp0 + "," +
                params.dsp1 + "," + params.dsv0 + "," + params.dsv1 + "," + params.dsv2 + "," + params.chof + "," + params.volq

        preciosService.ac_rbroV2(params.id, fecha.format("yyyy-MM-dd"), params.lugar)
        def res = preciosService.rb_preciosAsc(parametros, "")
        def vae = preciosService.rb_preciosVae(parametros, "")

        def prmsHeaderHoja = [border: Color.WHITE]
        def prmsFila = [border: Color.WHITE, align : Element.ALIGN_CENTER, valign: Element.ALIGN_MIDDLE]
        def prmsFilaIzquierda = [border: Color.WHITE, align : Element.ALIGN_LEFT, valign: Element.ALIGN_MIDDLE]
        def prmsFilaDerecha = [border: Color.WHITE, align : Element.ALIGN_RIGHT, valign: Element.ALIGN_MIDDLE]
        def prmsHeaderHoja2 = [border: Color.WHITE, colspan: 9]
        def prmsHeader = [border: Color.WHITE, colspan: 7, bg: new Color(73, 175, 205),
                          align : Element.ALIGN_CENTER, valign: Element.ALIGN_MIDDLE]
        def prmsHeader2 = [border: Color.WHITE, colspan: 3, bg: new Color(73, 175, 205),
                           align : Element.ALIGN_CENTER, valign: Element.ALIGN_MIDDLE]
        def prmsCellHead = [border: Color.WHITE, bg: new Color(73, 175, 205),
                            align : Element.ALIGN_CENTER, valign: Element.ALIGN_MIDDLE]
        def prmsCellCenter = [border: Color.BLACK, align: Element.ALIGN_CENTER, valign: Element.ALIGN_MIDDLE]
        def prmsCellRight = [border: Color.BLACK, align: Element.ALIGN_RIGHT, valign: Element.ALIGN_MIDDLE]
        def prmsCellLeft = [border: Color.BLACK, valign: Element.ALIGN_MIDDLE]
        def prmsSubtotal = [border: Color.BLACK, colspan: 6,
                            align : Element.ALIGN_RIGHT, valign: Element.ALIGN_MIDDLE]
        def prmsNum = [border: Color.BLACK, align: Element.ALIGN_RIGHT, valign: Element.ALIGN_MIDDLE]

//        def celdaCabecera = [border: Color.BLACK, bg: Color.LIGHT_GRAY, align: Element.ALIGN_CENTER, valign: Element.ALIGN_MIDDLE]
        def celdaCabecera = [border: Color.BLACK, bg: new Color(220, 220, 220), align: Element.ALIGN_CENTER, valign: Element.ALIGN_MIDDLE, bordeBot: "1"]
//        def celdaCabecera = [border: Color.BLACK, bg: Color.WHITE, align: Element.ALIGN_CENTER, valign: Element.ALIGN_MIDDLE, bordeBot: "1", bordeTop: "1"]
        def celdaCabeceraIzquierda = [bct: Color.BLACK, bcl: Color.WHITE, bcr:Color.WHITE, bcb: Color.WHITE, align: Element.ALIGN_LEFT, valign: Element.ALIGN_MIDDLE]
        def celdaCabeceraDerecha = [bct: Color.BLACK, bcl: Color.WHITE, bcr:Color.WHITE, bcb: Color.WHITE, align: Element.ALIGN_RIGHT, valign: Element.ALIGN_MIDDLE]
        def celdaCabeceraCentro = [bct: Color.BLACK, bcl: Color.WHITE, bcr:Color.WHITE, bcb: Color.WHITE, align: Element.ALIGN_CENTER, valign: Element.ALIGN_MIDDLE]
        def celdaCabeceraCentro2 = [bcb: Color.BLACK, bcl: Color.WHITE, bcr:Color.WHITE, bct: Color.WHITE, align: Element.ALIGN_CENTER, valign: Element.ALIGN_MIDDLE]
        def celdaCabeceraDerecha2 = [bcb: Color.BLACK, bcl: Color.WHITE, bcr:Color.WHITE, bct: Color.WHITE, align: Element.ALIGN_RIGHT, valign: Element.ALIGN_MIDDLE]
        def celdaCabeceraIzquierda2 = [bcb: Color.BLACK, bcl: Color.WHITE, bcr:Color.WHITE, bct: Color.WHITE, align: Element.ALIGN_LEFT, valign: Element.ALIGN_MIDDLE]

        def tituloRubro = [height: 20, border: Color.WHITE, colspan: 12, align : Element.ALIGN_LEFT, valign: Element.ALIGN_TOP]
        def tituloRubro13 = [height: 20, border: Color.WHITE, colspan: 13, align : Element.ALIGN_LEFT, valign: Element.ALIGN_TOP]
        def tituloRubro3 = [height: 20, border: Color.WHITE, colspan: 3, align : Element.ALIGN_LEFT, valign: Element.ALIGN_TOP]

        def prms = [prmsHeaderHoja: prmsHeaderHoja, prmsHeader: prmsHeader, prmsHeader2: prmsHeader2,
                    prmsCellHead: prmsCellHead, prmsCell: prmsCellCenter, prmsCellLeft: prmsCellLeft, prmsSubtotal: prmsSubtotal, prmsNum: prmsNum, prmsHeaderHoja2: prmsHeaderHoja2, prmsCellRight: prmsCellRight]

        Font times12bold = new Font(Font.TIMES_ROMAN, 12, Font.BOLD)
        Font times14bold = new Font(Font.TIMES_ROMAN, 14, Font.BOLD)
        Font times10bold = new Font(Font.TIMES_ROMAN, 10, Font.BOLD)
        Font times10normal = new Font(Font.TIMES_ROMAN, 10, Font.NORMAL)
        Font times8bold = new Font(Font.TIMES_ROMAN, 8, Font.BOLD)
        Font times8normal = new Font(Font.TIMES_ROMAN, 8, Font.NORMAL)
        Font times7bold = new Font(Font.TIMES_ROMAN, 7, Font.BOLD)
        Font times7normal = new Font(Font.TIMES_ROMAN, 7, Font.NORMAL)
        Font times10boldWhite = new Font(Font.TIMES_ROMAN, 10, Font.BOLD);
        Font times8boldWhite = new Font(Font.TIMES_ROMAN, 8, Font.BOLD)


        times8boldWhite.setColor(Color.WHITE)
        times10boldWhite.setColor(Color.WHITE)

        def fonts = [times12bold: times12bold, times10bold: times10bold, times8bold: times8bold,
                     times10boldWhite: times10boldWhite, times8boldWhite: times8boldWhite, times8normal: times8normal, times10normal: times10normal]

        def baos = new ByteArrayOutputStream()
        def name = "reporteRubros_" + new Date().format("ddMMyyyy_hhmm") + ".pdf";

        Document document
        document = new Document(PageSize.A4.rotate());
//        document.setMargins(marginLeft, marginRight, marginTop, marginBottom) 1/72 de pulgada, 1cm = 28.3
//        document.setMargins(60, 50, 45, 45)
        document.setMargins(70, 50, 45, 45);
        def pdfw = PdfWriter.getInstance(document, baos);
        document.open();
        document.addTitle("Rubros " + new Date().format("dd_MM_yyyy"));
        document.addSubject("Generado por el sistema Obras");
        document.addKeywords("documentosObra, janus, rubros");
        document.addAuthor("OBRAS");
        document.addCreator("Tedein SA");

        Paragraph headers = new Paragraph();
//        addEmptyLine(headers, 1);
        headers.setAlignment(Element.ALIGN_CENTER);
        headers.add(new Paragraph("G.A.D. LOS RÍOS", times14bold));
        headers.add(new Paragraph("COORDINACIÓN DE FIJACIÓN DE PRECIOS UNITARIOS - ANÁLISIS DE PRECIOS UNITARIOS", times10bold));
//        headers.add(new Paragraph("ANÁLISIS DE PRECIOS UNITARIOS", times10bold));
        headers.add(new Paragraph(" ", times10bold));
        document.add(headers)

        PdfPTable tablaCoeficiente = new PdfPTable(6);
        tablaCoeficiente.setWidthPercentage(100);
        tablaCoeficiente.setWidths(arregloEnteros([12,20, 15,30, 10,10]))

        reportesPdfService.addCellTb(tablaCoeficiente, new Paragraph("Código de rubro: ", times10bold), prmsHeaderHoja)
        reportesPdfService.addCellTb(tablaCoeficiente, new Paragraph((rubro?.codigo ?: ''), times10normal), prmsHeaderHoja)
        reportesPdfService.addCellTb(tablaCoeficiente, new Paragraph("Fecha: ", times10bold), [border: Color.WHITE, align: Element.ALIGN_RIGHT])
        reportesPdfService.addCellTb(tablaCoeficiente, new Paragraph((fecha1?.format("dd-MM-yyyy") ?: ''), times10normal), [border: Color.WHITE, align: Element.ALIGN_LEFT])
        reportesPdfService.addCellTb(tablaCoeficiente, new Paragraph("Fecha Act. P.U: ", times10bold), prmsHeaderHoja)
        reportesPdfService.addCellTb(tablaCoeficiente, new Paragraph((fecha?.format("dd-MM-yyyy") ?: '') , times10normal), prmsHeaderHoja)

        reportesPdfService.addCellTb(tablaCoeficiente, new Paragraph("Especificación: ", times10bold), prmsHeaderHoja)
        reportesPdfService.addCellTb(tablaCoeficiente, new Paragraph((rubro?.codigoEspecificacion ?: ''), times10normal), [border: Color.WHITE, colspan: 3])
        reportesPdfService.addCellTb(tablaCoeficiente, new Paragraph("Unidad: ", times10bold), prmsHeaderHoja)
        reportesPdfService.addCellTb(tablaCoeficiente, new Paragraph((rubro?.unidad?.codigo ?: ''), times10normal), prmsHeaderHoja)

        reportesPdfService.addCellTb(tablaCoeficiente, new Paragraph("Descripción: ", times10bold), prmsHeaderHoja)
        reportesPdfService.addCellTb(tablaCoeficiente, new Paragraph((rubro?.nombre ?: ''), times10normal), [border: Color.WHITE, colspan: 5])

        //EQUIPOS
        PdfPTable tablaEquipos = new PdfPTable(12);
        tablaEquipos.setWidthPercentage(100);
        tablaEquipos.setWidths(arregloEnteros([8,30,7,6,6,9,7,7,7,5,4,8]))

        reportesPdfService.addCellTb(tablaEquipos, new Paragraph("EQUIPOS", times12bold), tituloRubro)

        reportesPdfService.addCellTb(tablaEquipos, new Paragraph("CÓDIGO", times7bold), celdaCabecera)
        reportesPdfService.addCellTb(tablaEquipos, new Paragraph("DESCRIPCIÓN", times7bold), celdaCabecera)
        reportesPdfService.addCellTb(tablaEquipos, new Paragraph("CANTIDAD", times7bold), celdaCabecera)
        reportesPdfService.addCellTb(tablaEquipos, new Paragraph("TARIFA (\$/H)", times7bold), celdaCabecera)
        reportesPdfService.addCellTb(tablaEquipos, new Paragraph("COSTOS (\$)", times7bold), celdaCabecera)
        reportesPdfService.addCellTb(tablaEquipos, new Paragraph("RENDIMIENTO", times7bold), celdaCabecera)
        reportesPdfService.addCellTb(tablaEquipos, new Paragraph("C.TOTAL(\$)", times7bold), celdaCabecera)
        reportesPdfService.addCellTb(tablaEquipos, new Paragraph("PESO RELAT(%)", times7bold), celdaCabecera)
        reportesPdfService.addCellTb(tablaEquipos, new Paragraph("CPC", times7bold), celdaCabecera)
        reportesPdfService.addCellTb(tablaEquipos, new Paragraph("NP/EP/ ND", times7bold), celdaCabecera)
        reportesPdfService.addCellTb(tablaEquipos, new Paragraph("VAE (%)", times7bold), celdaCabecera)
        reportesPdfService.addCellTb(tablaEquipos, new Paragraph("VAE(%) ELEMENTO", times7bold), celdaCabecera)

        vae.eachWithIndex { r, i ->
            if (r["grpocdgo"] == 3) {
                reportesPdfService.addCellTb(tablaEquipos, new Paragraph(r["itemcdgo"], times8normal), prmsFilaIzquierda)
                reportesPdfService.addCellTb(tablaEquipos, new Paragraph(r["itemnmbr"], times8normal), prmsFilaIzquierda)
                reportesPdfService.addCellTb(tablaEquipos, new Paragraph(numero(r["rbrocntd"], 5)?.toString(), times8normal), prmsFila)
                reportesPdfService.addCellTb(tablaEquipos, new Paragraph(numero(r["rbpcpcun"], 5)?.toString(), times8normal), prmsFilaDerecha)
                reportesPdfService.addCellTb(tablaEquipos, new Paragraph((numero((r["rbpcpcun"] * r["rbrocntd"]), 5))?.toString(), times8normal), prmsFilaDerecha)
                reportesPdfService.addCellTb(tablaEquipos, new Paragraph(numero(r["rndm"], 5)?.toString(), times8normal), prmsFila)
                reportesPdfService.addCellTb(tablaEquipos, new Paragraph(numero(r["parcial"], 5)?.toString(), times8normal), prmsFilaDerecha)
                reportesPdfService.addCellTb(tablaEquipos, new Paragraph(numero(r["relativo"], 2)?.toString(), times8normal), prmsFila)
                reportesPdfService.addCellTb(tablaEquipos, new Paragraph(r["itemcpac"]?.toString(), times8normal), prmsFila)
                reportesPdfService.addCellTb(tablaEquipos, new Paragraph(r["tpbncdgo"], times8normal), prmsFila)
                reportesPdfService.addCellTb(tablaEquipos, new Paragraph(numero(r["vae"], 2)?.toString(), times8normal), prmsFila)
                reportesPdfService.addCellTb(tablaEquipos, new Paragraph((numero(r["vae_vlor"],2))?.toString(), times8normal), prmsFila)
                totalHer += r["parcial"]
                totalHerRel += r["relativo"]
                totalHerVae += r["vae_vlor"]
            }
        }

        reportesPdfService.addCellTb(tablaEquipos, new Paragraph("", times8bold), [border: Color.WHITE, colspan: 5])
        reportesPdfService.addCellTb(tablaEquipos, new Paragraph("TOTAL", times8bold), prmsFila)
        reportesPdfService.addCellTb(tablaEquipos, new Paragraph(numero(totalHer, 5)?.toString(), times8bold), prmsFilaDerecha)
        reportesPdfService.addCellTb(tablaEquipos, new Paragraph(numero(totalHerRel, 2)?.toString(), times8bold), prmsFila)
        reportesPdfService.addCellTb(tablaEquipos, new Paragraph("", times8bold), [border: Color.WHITE, colspan: 3])
        reportesPdfService.addCellTb(tablaEquipos, new Paragraph(numero(totalHerVae, 2)?.toString(), times8bold), prmsFila)

        //MANO DE OBRA
        PdfPTable tablaManoObra = new PdfPTable(12);
        tablaManoObra.setWidthPercentage(100);
        tablaManoObra.setWidths(arregloEnteros([6,32,7,6,6,9,7,7,7,5,4,8]))

        reportesPdfService.addCellTb(tablaManoObra, new Paragraph("MANO DE OBRA", times12bold), tituloRubro)

        reportesPdfService.addCellTb(tablaManoObra, new Paragraph("CÓDIGO", times7bold), celdaCabecera)
        reportesPdfService.addCellTb(tablaManoObra, new Paragraph("DESCRIPCIÓN", times7bold), celdaCabecera)
        reportesPdfService.addCellTb(tablaManoObra, new Paragraph("CANTIDAD", times7bold), celdaCabecera)
        reportesPdfService.addCellTb(tablaManoObra, new Paragraph("JORNAL (\$/H)", times7bold), celdaCabecera)
        reportesPdfService.addCellTb(tablaManoObra, new Paragraph("COSTOS (\$)", times7bold), celdaCabecera)
        reportesPdfService.addCellTb(tablaManoObra, new Paragraph("RENDIMIENTO", times7bold), celdaCabecera)
        reportesPdfService.addCellTb(tablaManoObra, new Paragraph("C.TOTAL (\$)", times7bold), celdaCabecera)
        reportesPdfService.addCellTb(tablaManoObra, new Paragraph("PESO RELAT(%)", times7bold), celdaCabecera)
        reportesPdfService.addCellTb(tablaManoObra, new Paragraph("CPC", times7bold), celdaCabecera)
        reportesPdfService.addCellTb(tablaManoObra, new Paragraph("NP/EP/ ND", times7bold), celdaCabecera)
        reportesPdfService.addCellTb(tablaManoObra, new Paragraph("VAE (%)", times7bold), celdaCabecera)
        reportesPdfService.addCellTb(tablaManoObra, new Paragraph("VAE(%) ELEMENTO", times7bold), celdaCabecera)

        vae.eachWithIndex { r, i ->
            if (r["grpocdgo"] == 2) {
                reportesPdfService.addCellTb(tablaManoObra, new Paragraph(r["itemcdgo"], times8normal), prmsFilaIzquierda)
                reportesPdfService.addCellTb(tablaManoObra, new Paragraph(r["itemnmbr"], times8normal), prmsFilaIzquierda)
                reportesPdfService.addCellTb(tablaManoObra, new Paragraph(numero(r["rbrocntd"], 5)?.toString(), times8normal), prmsFila)
                reportesPdfService.addCellTb(tablaManoObra, new Paragraph(numero(r["rbpcpcun"], 5)?.toString(), times8normal), prmsFilaDerecha)
                reportesPdfService.addCellTb(tablaManoObra, new Paragraph((numero((r["rbpcpcun"] * r["rbrocntd"]), 5))?.toString(), times8normal), prmsFilaDerecha)
                reportesPdfService.addCellTb(tablaManoObra, new Paragraph(numero(r["rndm"], 5)?.toString(), times8normal), prmsFila)
                reportesPdfService.addCellTb(tablaManoObra, new Paragraph(numero(r["parcial"], 5)?.toString(), times8normal), prmsFilaDerecha)
                reportesPdfService.addCellTb(tablaManoObra, new Paragraph(numero(r["relativo"], 2)?.toString(), times8normal), prmsFila)
                reportesPdfService.addCellTb(tablaManoObra, new Paragraph(r["itemcpac"]?.toString(), times8normal), prmsFila)
                reportesPdfService.addCellTb(tablaManoObra, new Paragraph(r["tpbncdgo"], times8normal), prmsFila)
                reportesPdfService.addCellTb(tablaManoObra, new Paragraph(numero(r["vae"], 2)?.toString(), times8normal), prmsFila)
                reportesPdfService.addCellTb(tablaManoObra, new Paragraph((numero(r["vae_vlor"],2))?.toString(), times8normal), prmsFila)
                totalMan += r["parcial"]
                totalManRel += r["relativo"]
                totalManVae += r["vae_vlor"]
            }
        }

        reportesPdfService.addCellTb(tablaManoObra, new Paragraph("", times8bold), [border: Color.WHITE, colspan: 5])
        reportesPdfService.addCellTb(tablaManoObra, new Paragraph("TOTAL", times8bold), prmsFila)
        reportesPdfService.addCellTb(tablaManoObra, new Paragraph(numero(totalMan, 5)?.toString(), times8bold), prmsFilaDerecha)
        reportesPdfService.addCellTb(tablaManoObra, new Paragraph(numero(totalManRel, 2)?.toString(), times8bold), prmsFila)
        reportesPdfService.addCellTb(tablaManoObra, new Paragraph("", times14bold), [border: Color.WHITE, colspan: 3])
        reportesPdfService.addCellTb(tablaManoObra, new Paragraph(numero(totalManVae, 2)?.toString(), times8bold), prmsFila)

        //MATERIALES
        PdfPTable tablaMateriales = new PdfPTable(11);
        tablaMateriales.setWidthPercentage(100);
        tablaMateriales.setWidths(arregloEnteros([8,37, 6,6,9,7,7,7,5,4,8]))

        if(params.trans == 'no'){
            reportesPdfService.addCellTb(tablaMateriales, new Paragraph("MATERIALES INCLUIDO TRANSPORTE", times12bold), tituloRubro)
        }else{
            reportesPdfService.addCellTb(tablaMateriales, new Paragraph("MATERIALES", times12bold), tituloRubro)
        }

        reportesPdfService.addCellTb(tablaMateriales, new Paragraph("CÓDIGO", times7bold), celdaCabecera)
        reportesPdfService.addCellTb(tablaMateriales, new Paragraph("DESCRIPCIÓN", times7bold), celdaCabecera)
        reportesPdfService.addCellTb(tablaMateriales, new Paragraph("UNIDAD", times7bold), celdaCabecera)
        reportesPdfService.addCellTb(tablaMateriales, new Paragraph("CANTI- DAD", times7bold), celdaCabecera)
        reportesPdfService.addCellTb(tablaMateriales, new Paragraph("UNITARIO(\$)", times7bold), celdaCabecera)
        reportesPdfService.addCellTb(tablaMateriales, new Paragraph("C.TOTAL(\$)", times7bold), celdaCabecera)
        reportesPdfService.addCellTb(tablaMateriales, new Paragraph("PESO RELAT(%)", times7bold), celdaCabecera)
        reportesPdfService.addCellTb(tablaMateriales, new Paragraph("CPC", times7bold), celdaCabecera)
        reportesPdfService.addCellTb(tablaMateriales, new Paragraph("NP/EP/ND", times7bold), celdaCabecera)
        reportesPdfService.addCellTb(tablaMateriales, new Paragraph("VAE(%)", times7bold), celdaCabecera)
        reportesPdfService.addCellTb(tablaMateriales, new Paragraph("VAE(%) ELEMENTO", times7bold), celdaCabecera)

        vae.eachWithIndex { r, i ->
            if (r["grpocdgo"] == 1) {
                bandMat = 1
                reportesPdfService.addCellTb(tablaMateriales, new Paragraph(r["itemcdgo"], times8normal), prmsFilaIzquierda)
                reportesPdfService.addCellTb(tablaMateriales, new Paragraph(r["itemnmbr"], times8normal), prmsFilaIzquierda)
                reportesPdfService.addCellTb(tablaMateriales, new Paragraph(r["unddcdgo"], times8normal), prmsFila)
                reportesPdfService.addCellTb(tablaMateriales, new Paragraph(numero(r["rbrocntd"], 5)?.toString(), times8normal), prmsFila)
                if (params.trans != 'no') {
                    reportesPdfService.addCellTb(tablaMateriales, new Paragraph(numero(r["rbpcpcun"], 5)?.toString(), times8normal), prmsFilaDerecha)
                    reportesPdfService.addCellTb(tablaMateriales, new Paragraph(numero(r["parcial"], 5)?.toString(), times8normal), prmsFilaDerecha)
                    reportesPdfService.addCellTb(tablaMateriales, new Paragraph(numero(r["relativo"], 2)?.toString(), times8normal), prmsFila)
                    reportesPdfService.addCellTb(tablaMateriales, new Paragraph(r["itemcpac"]?.toString(), times8normal), prmsFila)
                    reportesPdfService.addCellTb(tablaMateriales, new Paragraph(r["tpbncdgo"], times8normal), prmsFila)
                    reportesPdfService.addCellTb(tablaMateriales, new Paragraph(numero(r["vae"], 2)?.toString(), times8normal), prmsFila)
                    reportesPdfService.addCellTb(tablaMateriales, new Paragraph((numero(r["vae_vlor"],2))?.toString(), times8normal), prmsFila)
                    totalMat += r["parcial"]
                    totalMatRel += r["relativo"]
                    totalMatVae += r["vae_vlor"]
                }else{
                    reportesPdfService.addCellTb(tablaMateriales, new Paragraph(numero((r["rbpcpcun"] + r["parcial_t"] / r["rbrocntd"]), 5)?.toString(), times8normal), prmsFilaDerecha)
                    reportesPdfService.addCellTb(tablaMateriales, new Paragraph(numero((r["parcial"] + r["parcial_t"]), 5)?.toString(), times8normal), prmsFilaDerecha)
                    reportesPdfService.addCellTb(tablaMateriales, new Paragraph(numero((r["relativo"] + r["relativo_t"]), 2)?.toString(), times8normal), prmsFila)
                    reportesPdfService.addCellTb(tablaMateriales, new Paragraph(r["itemcpac"]?.toString(), times8normal), prmsFila)
                    reportesPdfService.addCellTb(tablaMateriales, new Paragraph(r["tpbncdgo"], times8normal), prmsFila)
                    reportesPdfService.addCellTb(tablaMateriales, new Paragraph(numero(r["vae"], 2)?.toString(), times8normal), prmsFila)
                    reportesPdfService.addCellTb(tablaMateriales, new Paragraph((numero(r["vae_vlor"] + r["vae_vlor_t"],2))?.toString(), times8normal), prmsFila)
                    totalMat += (r["parcial"] + r["parcial_t"])
                    totalMatRel += (r["relativo"] + r["relativo_t"])
                    totalMatVae += (r["vae_vlor"] + r["vae_vlor_t"])
                }

            }
        }

        reportesPdfService.addCellTb(tablaMateriales, new Paragraph("", times8bold), [border: Color.WHITE, colspan: 4])
        reportesPdfService.addCellTb(tablaMateriales, new Paragraph("TOTAL", times8bold), prmsFila)
        reportesPdfService.addCellTb(tablaMateriales, new Paragraph(numero(totalMat, 5)?.toString(), times8bold), prmsFilaDerecha)
        reportesPdfService.addCellTb(tablaMateriales, new Paragraph(numero(totalMatRel, 2)?.toString(), times8bold), prmsFila)
        reportesPdfService.addCellTb(tablaMateriales, new Paragraph("", times8bold), [border: Color.WHITE, colspan: 3])
        reportesPdfService.addCellTb(tablaMateriales, new Paragraph(numero(totalMatVae, 2)?.toString(), times8bold), prmsFila)

        //MATERIALES VACIA
        PdfPTable tablaMaterialesVacia = new PdfPTable(11);
        tablaMaterialesVacia.setWidthPercentage(100);
        tablaMaterialesVacia.setWidths(arregloEnteros([8,37,6,6,9,7,7,7,5,4,8]))

        reportesPdfService.addCellTb(tablaMaterialesVacia, new Paragraph("MATERIALES", times12bold), tituloRubro)

        reportesPdfService.addCellTb(tablaMaterialesVacia, new Paragraph("CÓDIGO", times7bold), celdaCabecera)
        reportesPdfService.addCellTb(tablaMaterialesVacia, new Paragraph("DESCRIPCIÓN", times7bold), celdaCabecera)
        reportesPdfService.addCellTb(tablaMaterialesVacia, new Paragraph("UNIDAD", times7bold), celdaCabecera)
        reportesPdfService.addCellTb(tablaMaterialesVacia, new Paragraph("CANTI- DAD", times7bold), celdaCabecera)
        reportesPdfService.addCellTb(tablaMaterialesVacia, new Paragraph("UNITARIO (\$)", times7bold), celdaCabecera)
        reportesPdfService.addCellTb(tablaMaterialesVacia, new Paragraph("C.TOTAL (\$)", times7bold), celdaCabecera)
        reportesPdfService.addCellTb(tablaMaterialesVacia, new Paragraph("PESO RELAT(%)", times7bold), celdaCabecera)
        reportesPdfService.addCellTb(tablaMaterialesVacia, new Paragraph("CPC", times7bold), celdaCabecera)
        reportesPdfService.addCellTb(tablaMaterialesVacia, new Paragraph("NP/EP/ ND", times7bold), celdaCabecera)
        reportesPdfService.addCellTb(tablaMaterialesVacia, new Paragraph("VAE(%)", times7bold), celdaCabecera)
        reportesPdfService.addCellTb(tablaMaterialesVacia, new Paragraph("VAE(%) ELEMENTO", times7bold), celdaCabecera)

        //TRANSPORTE
        PdfPTable tablaTransporte = new PdfPTable(13);
        tablaTransporte.setWidthPercentage(100);
        tablaTransporte.setWidths(arregloEnteros([8,27,4,6,6,6,9,7,7,7,5,4,8]))

        reportesPdfService.addCellTb(tablaTransporte, new Paragraph("TRANSPORTE", times12bold), tituloRubro13)

        reportesPdfService.addCellTb(tablaTransporte, new Paragraph("CÓDIGO", times7bold), celdaCabecera)
        reportesPdfService.addCellTb(tablaTransporte, new Paragraph("DESCRIPCIÓN", times7bold), celdaCabecera)
        reportesPdfService.addCellTb(tablaTransporte, new Paragraph("UNI- DAD", times7bold), celdaCabecera)
        reportesPdfService.addCellTb(tablaTransporte, new Paragraph("PES/VOL", times7bold), celdaCabecera)
        reportesPdfService.addCellTb(tablaTransporte, new Paragraph("CANT.", times7bold), celdaCabecera)
        reportesPdfService.addCellTb(tablaTransporte, new Paragraph("DISTAN- CIA", times7bold), celdaCabecera)
        reportesPdfService.addCellTb(tablaTransporte, new Paragraph("TARIFA", times7bold), celdaCabecera)
        reportesPdfService.addCellTb(tablaTransporte, new Paragraph("C.TOTAL(\$)", times7bold), celdaCabecera)
        reportesPdfService.addCellTb(tablaTransporte, new Paragraph("PESO RELAT(%)", times7bold), celdaCabecera)
        reportesPdfService.addCellTb(tablaTransporte, new Paragraph("CPC", times7bold), celdaCabecera)
        reportesPdfService.addCellTb(tablaTransporte, new Paragraph("NP/EP/ ND", times7bold), celdaCabecera)
        reportesPdfService.addCellTb(tablaTransporte, new Paragraph("VAE(%)", times7bold), celdaCabecera)
        reportesPdfService.addCellTb(tablaTransporte, new Paragraph("VAE(%) ELEMENTO", times7bold), celdaCabecera)

        vae.eachWithIndex { r, i ->
            if (r["grpocdgo"]== 1 && params.trans != 'no') {
                reportesPdfService.addCellTb(tablaTransporte, new Paragraph(r["itemcdgo"], times8normal), prmsFilaIzquierda)
                reportesPdfService.addCellTb(tablaTransporte, new Paragraph(r["itemnmbr"], times8normal), prmsFilaIzquierda)
                if(r["tplscdgo"].trim() =='P' || r["tplscdgo"].trim() =='P1' ){
                    reportesPdfService.addCellTb(tablaTransporte, new Paragraph("ton-km", times8normal), prmsFila)
                }else{
                    if(r["tplscdgo"].trim() =='V' || r["tplscdgo"].trim() =='V1' || r["tplscdgo"].trim() =='V2') {
                        reportesPdfService.addCellTb(tablaTransporte, new Paragraph("m3-km", times8normal), prmsFila)
                    }else{
                        reportesPdfService.addCellTb(tablaTransporte, new Paragraph(r["unddcdgo"], times8normal), prmsFila)
                    }
                }
                reportesPdfService.addCellTb(tablaTransporte, new Paragraph(numero(r["itempeso"], 5)?.toString(), times8normal), prmsFila)
                reportesPdfService.addCellTb(tablaTransporte, new Paragraph(numero(r["rbrocntd"], 5)?.toString(), times8normal), prmsFila)
                reportesPdfService.addCellTb(tablaTransporte, new Paragraph(numero(r["distancia"], 5)?.toString(), times8normal), prmsFila)
                reportesPdfService.addCellTb(tablaTransporte, new Paragraph(numero(r["tarifa"], 5)?.toString(), times8normal), prmsFila)
                reportesPdfService.addCellTb(tablaTransporte, new Paragraph(numero(r["parcial_t"], 5)?.toString(), times8normal), prmsFilaDerecha)
                reportesPdfService.addCellTb(tablaTransporte, new Paragraph(numero(r["relativo_t"], 2)?.toString(), times8normal), prmsFilaDerecha)
                reportesPdfService.addCellTb(tablaTransporte, new Paragraph((r["itemcpac"] ?: '')?.toString(), times8normal), prmsFila)
                reportesPdfService.addCellTb(tablaTransporte, new Paragraph(r["tpbncdgo"], times8normal), prmsFila)
                reportesPdfService.addCellTb(tablaTransporte, new Paragraph(numero(r["vae_t"], 2)?.toString(), times8normal), prmsFila)
                reportesPdfService.addCellTb(tablaTransporte, new Paragraph(numero(r["vae_vlor_t"], 2)?.toString(), times8normal), prmsFila)
                total += r["parcial_t"]
                totalTRel += r["relativo_t"]
                totalTVae += r["vae_vlor_t"]
            }
        }

        reportesPdfService.addCellTb(tablaTransporte, new Paragraph("", times8bold), [border: Color.WHITE, colspan: 6])
        reportesPdfService.addCellTb(tablaTransporte, new Paragraph("TOTAL", times8bold), prmsFila)
        reportesPdfService.addCellTb(tablaTransporte, new Paragraph(numero(total, 5)?.toString(), times8bold), prmsFilaDerecha)
        reportesPdfService.addCellTb(tablaTransporte, new Paragraph(numero(totalTRel, 2)?.toString(), times8bold), prmsFilaDerecha)
        reportesPdfService.addCellTb(tablaTransporte, new Paragraph("", times14bold), [border: Color.WHITE, colspan: 3])
        reportesPdfService.addCellTb(tablaTransporte, new Paragraph(numero(totalTVae, 2)?.toString(), times8bold), prmsFila)

        //TRANSPORTE VACIA

        PdfPTable tablaTransporteVacia = new PdfPTable(13);
        tablaTransporteVacia.setWidthPercentage(100);
        tablaTransporteVacia.setWidths(arregloEnteros([8,27,4,6,6,6,9,7,7,7,5,4,8]))

        reportesPdfService.addCellTb(tablaTransporteVacia, new Paragraph("TRANSPORTE", times12bold), tituloRubro13)

        reportesPdfService.addCellTb(tablaTransporteVacia, new Paragraph("CÓDIGO", times7bold), celdaCabecera)
        reportesPdfService.addCellTb(tablaTransporteVacia, new Paragraph("DESCRIPCIÓN", times7bold), celdaCabecera)
        reportesPdfService.addCellTb(tablaTransporteVacia, new Paragraph("UNIDAD", times7bold), celdaCabecera)
        reportesPdfService.addCellTb(tablaTransporteVacia, new Paragraph("PES/VOL", times7bold), celdaCabecera)
        reportesPdfService.addCellTb(tablaTransporteVacia, new Paragraph("CANTIDAD", times7bold), celdaCabecera)
        reportesPdfService.addCellTb(tablaTransporteVacia, new Paragraph("DISTANCIA", times7bold), celdaCabecera)
        reportesPdfService.addCellTb(tablaTransporteVacia, new Paragraph("TARIFA", times7bold), celdaCabecera)
        reportesPdfService.addCellTb(tablaTransporteVacia, new Paragraph("C.TOTAL(\$)", times7bold), celdaCabecera)
        reportesPdfService.addCellTb(tablaTransporteVacia, new Paragraph("PESO RELAT(%)", times7bold), celdaCabecera)
        reportesPdfService.addCellTb(tablaTransporteVacia, new Paragraph("CPC", times7bold), celdaCabecera)
        reportesPdfService.addCellTb(tablaTransporteVacia, new Paragraph("NP/EP/ND", times7bold), celdaCabecera)
        reportesPdfService.addCellTb(tablaTransporteVacia, new Paragraph("VAE(%)", times7bold), celdaCabecera)
        reportesPdfService.addCellTb(tablaTransporteVacia, new Paragraph("VAE(%) ELEMENTO", times7bold), celdaCabecera)

        //COSTOS INDIRECTOS
        def totalRubro = total + totalHer + totalMan + totalMat
        def totalRelativo = totalTRel + totalHerRel + totalMatRel + totalManRel
        def totalVae = totalTVae + totalHerVae + totalMatVae + totalManVae
        def totalIndi = totalRubro?.toDouble() * indi / 100

        PdfPTable tablaIndirectos = new PdfPTable(3);
        tablaIndirectos.setWidthPercentage(70);
        tablaIndirectos.setWidths(arregloEnteros([50,25,25]))
        tablaIndirectos.horizontalAlignment = Element.ALIGN_LEFT;

        reportesPdfService.addCellTb(tablaIndirectos, new Paragraph("COSTOS INDIRECTOS",  times12bold), tituloRubro3)

        reportesPdfService.addCellTb(tablaIndirectos, new Paragraph("DESCRIPCIÓN", times8bold), celdaCabecera)
        reportesPdfService.addCellTb(tablaIndirectos, new Paragraph("PORCENTAJE", times8bold), celdaCabecera)
        reportesPdfService.addCellTb(tablaIndirectos, new Paragraph("VALOR", times8bold), celdaCabecera)

        reportesPdfService.addCellTb(tablaIndirectos, new Paragraph("COSTOS INDIRECTOS", times8normal), prmsFilaIzquierda)
        reportesPdfService.addCellTb(tablaIndirectos, new Paragraph(numero(indi, 1)?.toString() + "%", times8normal), prmsFila)
        reportesPdfService.addCellTb(tablaIndirectos, new Paragraph(numero(totalIndi, 5)?.toString(), times8normal), prmsFila)

        if(rubro?.codigo?.split("-")[0] == 'TR'){
            reportesPdfService.addCellTb(tablaIndirectos, new Paragraph("Distancia a la escombrera: ${obra?.distanciaDesalojo ?: '0'} KM", times8bold),
                    [border: Color.WHITE, colspan: 3, align : Element.ALIGN_LEFT, valign: Element.ALIGN_LEFT])
        }


        reportesPdfService.addCellTb(tablaIndirectos, new Paragraph("", times8bold), [border: Color.WHITE, colspan: 3])
        reportesPdfService.addCellTb(tablaIndirectos, new Paragraph("", times8bold), [border: Color.WHITE, colspan: 3])

        PdfPTable tablaTotales = new PdfPTable(4);
        tablaTotales.setWidthPercentage(70);
        tablaTotales.setWidths(arregloEnteros([30,25,25,20]))
        tablaTotales.horizontalAlignment = Element.ALIGN_RIGHT;

        reportesPdfService.addCellTb(tablaTotales, new Paragraph("COSTO UNITARIO DIRECTO", times8bold), celdaCabeceraIzquierda)
        reportesPdfService.addCellTb(tablaTotales, new Paragraph(numero(totalRubro, 2)?.toString(), times8bold), celdaCabeceraDerecha)
        reportesPdfService.addCellTb(tablaTotales, new Paragraph(numero(totalRelativo, 2)?.toString(), times8bold), celdaCabeceraCentro)
        reportesPdfService.addCellTb(tablaTotales, new Paragraph(numero(totalVae, 2)?.toString(), times8bold), celdaCabeceraCentro)

        reportesPdfService.addCellTb(tablaTotales, new Paragraph("COSTOS INDIRECTO", times8bold), prmsFilaIzquierda)
        reportesPdfService.addCellTb(tablaTotales, new Paragraph(numero(totalIndi, 2)?.toString(), times8bold), prmsFilaDerecha)
        reportesPdfService.addCellTb(tablaTotales, new Paragraph("TOTAL", times8bold), prmsFila)
        reportesPdfService.addCellTb(tablaTotales, new Paragraph("TOTAL", times8bold), prmsFila)

        reportesPdfService.addCellTb(tablaTotales, new Paragraph("COSTO TOTAL DEL RUBRO", times8bold), prmsFilaIzquierda)
        reportesPdfService.addCellTb(tablaTotales, new Paragraph(numero((totalRubro + totalIndi), 2)?.toString(), times8bold), prmsFilaDerecha)
        reportesPdfService.addCellTb(tablaTotales, new Paragraph("PESO", times8bold), prmsFila)
        reportesPdfService.addCellTb(tablaTotales, new Paragraph("VAE", times8bold), prmsFila)

        reportesPdfService.addCellTb(tablaTotales, new Paragraph("PRECIO UNITARIO \$USD", times8bold), celdaCabeceraIzquierda2)
        reportesPdfService.addCellTb(tablaTotales, new Paragraph(numero((totalRubro + totalIndi), 2)?.toString(), times8bold), celdaCabeceraDerecha2)
        reportesPdfService.addCellTb(tablaTotales, new Paragraph("RELATIVO", times8bold), celdaCabeceraCentro2)
        reportesPdfService.addCellTb(tablaTotales, new Paragraph("(%)", times8bold), celdaCabeceraCentro2)

        document.add(tablaCoeficiente)
        document.add(tablaEquipos)
        document.add(tablaManoObra)
        document.add(tablaMateriales)
//        if(bandMat != 1){
//            document.add(tablaMaterialesVacia)
//        }
        if (total == 0 || params.trans == "no"){
        }else{
            document.add(tablaTransporte)
        }
        if(band == 0 && bandTrans == '1'){
            document.add(tablaTransporteVacia)
        }
        document.add(tablaIndirectos)
        document.add(tablaTotales)

        PdfPTable tablaNota = new PdfPTable(2);
        tablaNota.setWidthPercentage(100);
        tablaNota.setWidths(arregloEnteros([4, 96]))
        reportesPdfService.addCellTb(tablaNota, new Paragraph("Nota:", times8bold), prmsFilaIzquierda)
        reportesPdfService.addCellTb(tablaNota, new Paragraph("Los cálculos se hacen con todos los " +
                "decimales y el resultado final se lo redondea a dos decimales.", times8normal), prmsFilaIzquierda)
        document.add(tablaNota)

        document.close();
        pdfw.close()
        byte[] b = baos.toByteArray();
        response.setContentType("application/pdf")
        response.setHeader("Content-disposition", "attachment; filename=" + name)
        response.setContentLength(b.length)
        response.getOutputStream().write(b)
    }

    def reporteRubrosTransporteRegistro(){
        println("params rrtr " + params)

        def obra = Obra.get(params.obra)
        def fecha1
        def fecha2
        def rubros = []
        def lugar = obra?.lugar
        def indi = obra?.totales

        if(obra?.fechaPreciosRubros) {
            fecha1 = obra?.fechaPreciosRubros
        }

        if(obra?.fechaOficioSalida) {
            fecha2 = obra?.fechaOficioSalida
        }

        if(obra.estado != 'R') {
            println "antes de imprimir rubros.. actualiza desalojo y herramienta menor"
            preciosService.ac_transporteDesalojo(obra.id)
            preciosService.ac_rbroObra(obra.id)
        }

        rubros = VolumenesObra.findAllByObra(obra, [sort: "orden"]).item.unique()

        def bandMat = 0
        def band = 0
        def bandTrans = params.desglose

        try {
            indi = indi.toDouble()
        } catch (e) {
            println "error parse " + e
            indi = 21.5
        }

        def prmsHeaderHoja = [border: Color.WHITE]
        def prmsFila = [border: Color.WHITE, align: Element.ALIGN_CENTER, valign: Element.ALIGN_MIDDLE]
        def prmsFilaIzquierda = [border: Color.WHITE, align : Element.ALIGN_LEFT, valign: Element.ALIGN_LEFT]
        def prmsFilaDerecha = [border: Color.WHITE, align : Element.ALIGN_RIGHT, valign: Element.ALIGN_RIGHT]
        def prmsHeaderHoja2 = [border: Color.WHITE, colspan: 9]
        def prmsHeader = [border: Color.WHITE, colspan: 7, bg: new Color(73, 175, 205),
                          align : Element.ALIGN_CENTER, valign: Element.ALIGN_MIDDLE]
        def prmsHeader2 = [border: Color.WHITE, colspan: 3, bg: new Color(73, 175, 205),
                           align : Element.ALIGN_CENTER, valign: Element.ALIGN_MIDDLE]
        def prmsCellHead = [border: Color.WHITE, bg: new Color(73, 175, 205),
                            align : Element.ALIGN_CENTER, valign: Element.ALIGN_MIDDLE]
        def prmsCellCenter = [border: Color.BLACK, align: Element.ALIGN_CENTER, valign: Element.ALIGN_MIDDLE]
        def prmsCellRight = [border: Color.BLACK, align: Element.ALIGN_RIGHT, valign: Element.ALIGN_RIGHT]
        def prmsCellLeft = [border: Color.BLACK, valign: Element.ALIGN_MIDDLE]
        def prmsSubtotal = [border: Color.BLACK, colspan: 6,
                            align : Element.ALIGN_RIGHT, valign: Element.ALIGN_MIDDLE]
        def prmsNum = [border: Color.BLACK, align: Element.ALIGN_RIGHT, valign: Element.ALIGN_MIDDLE]

        def celdaCabecera = [border: Color.BLACK, bg: new Color(220, 220, 220), align: Element.ALIGN_CENTER, valign: Element.ALIGN_MIDDLE, bordeBot: "1"]
//        def celdaCabecera = [bg: new Color(220, 220, 220), align: Element.ALIGN_CENTER, valign: Element.ALIGN_MIDDLE, bordeBot: "1"]

        def celdaCabeceraIzquierda = [bct: Color.BLACK, bcl: Color.WHITE, bcr:Color.WHITE, bcb: Color.WHITE, align: Element.ALIGN_LEFT, valign: Element.ALIGN_LEFT]
        def celdaCabeceraDerecha = [bct: Color.BLACK, bcl: Color.WHITE, bcr:Color.WHITE, bcb: Color.WHITE, align: Element.ALIGN_RIGHT, valign: Element.ALIGN_RIGHT]
        def celdaCabeceraCentro = [bct: Color.BLACK, bcl: Color.WHITE, bcr:Color.WHITE, bcb: Color.WHITE, align: Element.ALIGN_CENTER, valign: Element.ALIGN_CENTER]
        def celdaCabeceraCentro2 = [bcb: Color.BLACK, bcl: Color.WHITE, bcr:Color.WHITE, bct: Color.WHITE, align: Element.ALIGN_CENTER, valign: Element.ALIGN_CENTER]
        def celdaCabeceraDerecha2 = [bcb: Color.BLACK, bcl: Color.WHITE, bcr:Color.WHITE, bct: Color.WHITE, align: Element.ALIGN_RIGHT, valign: Element.ALIGN_RIGHT]
        def celdaCabeceraIzquierda2 = [bcb: Color.BLACK, bcl: Color.WHITE, bcr:Color.WHITE, bct: Color.WHITE, align: Element.ALIGN_LEFT, valign: Element.ALIGN_LEFT]

        def tituloRubro = [height: 25, border: Color.WHITE, colspan: 12, align : Element.ALIGN_LEFT, valign: Element.ALIGN_MIDDLE]

        def prms = [prmsHeaderHoja: prmsHeaderHoja, prmsHeader: prmsHeader, prmsHeader2: prmsHeader2,
                    prmsCellHead: prmsCellHead, prmsCell: prmsCellCenter, prmsCellLeft: prmsCellLeft, prmsSubtotal: prmsSubtotal, prmsNum: prmsNum, prmsHeaderHoja2: prmsHeaderHoja2, prmsCellRight: prmsCellRight]

        Font times12bold = new Font(Font.TIMES_ROMAN, 12, Font.BOLD);
        Font times14bold = new Font(Font.TIMES_ROMAN, 14, Font.BOLD);
        Font times10bold = new Font(Font.TIMES_ROMAN, 10, Font.BOLD);
        Font times10normal = new Font(Font.TIMES_ROMAN, 10, Font.NORMAL);
        Font times8bold = new Font(Font.TIMES_ROMAN, 8, Font.BOLD)
        Font times8normal = new Font(Font.TIMES_ROMAN, 8, Font.NORMAL)
        Font times7bold = new Font(Font.TIMES_ROMAN, 7, Font.BOLD)
        Font times7normal = new Font(Font.TIMES_ROMAN, 7, Font.NORMAL)
        Font times10boldWhite = new Font(Font.TIMES_ROMAN, 10, Font.BOLD);
        Font times8boldWhite = new Font(Font.TIMES_ROMAN, 8, Font.BOLD)
        times8boldWhite.setColor(Color.WHITE)
        times10boldWhite.setColor(Color.WHITE)

        def baos = new ByteArrayOutputStream()
        def name = "reporteRubrosTransporte_" + new Date().format("ddMMyyyy_hhmm") + ".pdf";

        Document document
        document = new Document(PageSize.A4)
        document.setMargins(60, 24, 45, 45);

        def pdfw = PdfWriter.getInstance(document, baos);
        document.open();
        document.addTitle("Rubros " + new Date().format("dd_MM_yyyy"));
        document.addSubject("Generado por el sistema Obras");
        document.addKeywords("documentosObra, janus, rubros");
        document.addAuthor("OBRAS");
        document.addCreator("Tedein SA");

        rubros.eachWithIndex{ rubro, indice->

            document.newPage();

            def nombre = rubro?.nombre

            preciosService.ac_rbroObra(obra.id)
            def res = preciosService.precioUnitarioVolumenObraAsc("*", obra.id, rubro.id)

            def total = 0, totalHer = 0, totalMan = 0, totalMat = 0

            Paragraph headers = new Paragraph();
            addEmptyLine(headers, 1);
            headers.setAlignment(Element.ALIGN_CENTER);
            headers.add(new Paragraph("G.A.D. LOS RÍOS", times14bold));
            headers.add(new Paragraph("COORDINACIÓN DE FIJACIÓN DE PRECIOS UNITARIOS - ANÁLISIS DE PRECIOS UNITARIOS", times10bold));
            headers.add(new Paragraph("", times14bold));

            PdfPTable tablaCoeficiente = new PdfPTable(4);
            tablaCoeficiente.setWidthPercentage(100);
            tablaCoeficiente.setWidths(arregloEnteros([17,33, 17,33]))

            reportesPdfService.addCellTb(tablaCoeficiente, new Paragraph("Fecha: ", times10bold), prmsHeaderHoja)
            reportesPdfService.addCellTb(tablaCoeficiente, new Paragraph((fecha2?.format("dd-MM-yyyy") ?: ''), times10normal), prmsHeaderHoja)
            reportesPdfService.addCellTb(tablaCoeficiente, new Paragraph("Fecha Act. P.U: ", times10bold), prmsHeaderHoja)
            reportesPdfService.addCellTb(tablaCoeficiente, new Paragraph((fecha1?.format("dd-MM-yyyy") ?: '') , times10normal), prmsHeaderHoja)

            reportesPdfService.addCellTb(tablaCoeficiente, new Paragraph("Código de obra: ", times10bold), prmsHeaderHoja)
            reportesPdfService.addCellTb(tablaCoeficiente, new Paragraph((obra?.codigo ?: ''), times10normal), [border: Color.WHITE, colspan: 3])

            reportesPdfService.addCellTb(tablaCoeficiente, new Paragraph("Presupuesto: ", times10bold), prmsHeaderHoja)
            reportesPdfService.addCellTb(tablaCoeficiente, new Paragraph((obra?.nombre ?: ''), times10normal), [border: Color.WHITE, colspan: 3])

            reportesPdfService.addCellTb(tablaCoeficiente, new Paragraph("Código de rubro: ", times10bold), prmsHeaderHoja)
            reportesPdfService.addCellTb(tablaCoeficiente, new Paragraph((rubro?.codigo ?: ''), times10normal), prmsHeaderHoja)
            reportesPdfService.addCellTb(tablaCoeficiente, new Paragraph("Unidad: ", times10bold), prmsHeaderHoja)
            reportesPdfService.addCellTb(tablaCoeficiente, new Paragraph((rubro?.unidad?.codigo ?: ''), times10normal), prmsHeaderHoja)

            reportesPdfService.addCellTb(tablaCoeficiente, new Paragraph("Descripción: ", times10bold), prmsHeaderHoja)
            reportesPdfService.addCellTb(tablaCoeficiente, new Paragraph((nombre ?: ''), times10normal), [border: Color.WHITE, colspan: 5])

            //EQUIPOS
            PdfPTable tablaEquipos = new PdfPTable(7);
            tablaEquipos.setWidthPercentage(100);
            tablaEquipos.setWidths(arregloEnteros([8,40,8,9,8,10,8]))

            reportesPdfService.addCellTb(tablaEquipos, new Paragraph("EQUIPOS", times12bold), tituloRubro)

            reportesPdfService.addCellTb(tablaEquipos, new Paragraph("CÓDIGO", times7bold), celdaCabecera)
            reportesPdfService.addCellTb(tablaEquipos, new Paragraph("DESCRIPCIÓN", times7bold), celdaCabecera)
            reportesPdfService.addCellTb(tablaEquipos, new Paragraph("CANTIDAD", times7bold), celdaCabecera)
            reportesPdfService.addCellTb(tablaEquipos, new Paragraph("TARIFA(\$/H)", times7bold), celdaCabecera)
            reportesPdfService.addCellTb(tablaEquipos, new Paragraph("COSTOS(\$)", times7bold), celdaCabecera)
            reportesPdfService.addCellTb(tablaEquipos, new Paragraph("RENDIMIENTO", times7bold), celdaCabecera)
            reportesPdfService.addCellTb(tablaEquipos, new Paragraph("C.TOTAL(\$)", times7bold), celdaCabecera)

            res.eachWithIndex { r, i ->
                if (r["grpocdgo"] == 3) {
                    reportesPdfService.addCellTb(tablaEquipos, new Paragraph(r["itemcdgo"], times8normal), prmsFilaIzquierda)
                    reportesPdfService.addCellTb(tablaEquipos, new Paragraph(r["itemnmbr"], times8normal), prmsFilaIzquierda)
                    reportesPdfService.addCellTb(tablaEquipos, new Paragraph(numero(r["rbrocntd"], 5)?.toString(), times8normal), prmsFilaDerecha)
                    reportesPdfService.addCellTb(tablaEquipos, new Paragraph(numero(r["rbpcpcun"], 5)?.toString(), times8normal), prmsFilaDerecha)
                    reportesPdfService.addCellTb(tablaEquipos, new Paragraph((numero((r["rbpcpcun"] * r["rbrocntd"]), 5))?.toString(), times8normal), prmsFilaDerecha)
                    reportesPdfService.addCellTb(tablaEquipos, new Paragraph(numero(r["rndm"], 5)?.toString(), times8normal), prmsFilaDerecha)
                    reportesPdfService.addCellTb(tablaEquipos, new Paragraph(numero(r["parcial"], 5)?.toString(), times8normal), prmsFilaDerecha)
                    totalHer += r["parcial"]
                }
            }

            reportesPdfService.addCellTb(tablaEquipos, new Paragraph("", times14bold), [border: Color.WHITE, colspan: 5])
            reportesPdfService.addCellTb(tablaEquipos, new Paragraph("TOTAL", times8bold), prmsFilaDerecha)
            reportesPdfService.addCellTb(tablaEquipos, new Paragraph(numero(totalHer, 5)?.toString(), times8bold), prmsFilaDerecha)
            reportesPdfService.addCellTb(tablaEquipos, new Paragraph("", times14bold), [border: Color.WHITE, colspan: 7])

            //MANO DE OBRA
            PdfPTable tablaManoObra = new PdfPTable(7);
            tablaManoObra.setWidthPercentage(100);
            tablaManoObra.setWidths(arregloEnteros([6,42,8,9,8,10,8]))

            reportesPdfService.addCellTb(tablaManoObra, new Paragraph("MANO DE OBRA", times12bold), tituloRubro)

            reportesPdfService.addCellTb(tablaManoObra, new Paragraph("CÓDIGO", times7bold), celdaCabecera)
            reportesPdfService.addCellTb(tablaManoObra, new Paragraph("DESCRIPCIÓN", times7bold), celdaCabecera)
            reportesPdfService.addCellTb(tablaManoObra, new Paragraph("CANTIDAD", times7bold), celdaCabecera)
            reportesPdfService.addCellTb(tablaManoObra, new Paragraph("JORNAL(\$/H)", times7bold), celdaCabecera)
            reportesPdfService.addCellTb(tablaManoObra, new Paragraph("COSTOS(\$)", times7bold), celdaCabecera)
            reportesPdfService.addCellTb(tablaManoObra, new Paragraph("RENDIMIENTO", times7bold), celdaCabecera)
            reportesPdfService.addCellTb(tablaManoObra, new Paragraph("C.TOTAL(\$)", times7bold), celdaCabecera)

            res.eachWithIndex { r, i ->
                if (r["grpocdgo"] == 2) {
                    reportesPdfService.addCellTb(tablaManoObra, new Paragraph(r["itemcdgo"], times8normal), prmsFilaIzquierda)
                    reportesPdfService.addCellTb(tablaManoObra, new Paragraph(r["itemnmbr"], times8normal), prmsFilaIzquierda)
                    reportesPdfService.addCellTb(tablaManoObra, new Paragraph(numero(r["rbrocntd"], 5)?.toString(), times8normal), prmsFilaDerecha)
                    reportesPdfService.addCellTb(tablaManoObra, new Paragraph(numero(r["rbpcpcun"], 5)?.toString(), times8normal), prmsFilaDerecha)
                    reportesPdfService.addCellTb(tablaManoObra, new Paragraph((numero((r["rbpcpcun"] * r["rbrocntd"]), 5))?.toString(), times8normal), prmsFilaDerecha)
                    reportesPdfService.addCellTb(tablaManoObra, new Paragraph(numero(r["rndm"], 5)?.toString(), times8normal), prmsFilaDerecha)
                    reportesPdfService.addCellTb(tablaManoObra, new Paragraph(numero(r["parcial"], 5)?.toString(), times8normal), prmsFilaDerecha)
                    totalMan += r["parcial"]
                }
            }

            reportesPdfService.addCellTb(tablaManoObra, new Paragraph("", times8bold), [border: Color.WHITE, colspan: 5])
            reportesPdfService.addCellTb(tablaManoObra, new Paragraph("TOTAL", times8bold), prmsFilaDerecha)
            reportesPdfService.addCellTb(tablaManoObra, new Paragraph(numero(totalMan, 5)?.toString(), times8bold), prmsFilaDerecha)
            reportesPdfService.addCellTb(tablaManoObra, new Paragraph("", times8bold), [border: Color.WHITE, colspan: 7])

            //MATERIALES
            PdfPTable tablaMateriales = new PdfPTable(6);
            tablaMateriales.setWidthPercentage(100);
            tablaMateriales.setWidths(arregloEnteros([8,48,9,8,10,8]))

            if(params.desglose == '0'){
                reportesPdfService.addCellTb(tablaMateriales, new Paragraph("MATERIALES INCLUIDO TRANSPORTE", times12bold), tituloRubro)
            }else{
                reportesPdfService.addCellTb(tablaMateriales, new Paragraph("MATERIALES", times12bold), tituloRubro)
            }

            reportesPdfService.addCellTb(tablaMateriales, new Paragraph("CÓDIGO", times7bold), celdaCabecera)
            reportesPdfService.addCellTb(tablaMateriales, new Paragraph("DESCRIPCIÓN", times7bold), celdaCabecera)
            reportesPdfService.addCellTb(tablaMateriales, new Paragraph("UNIDAD", times7bold), celdaCabecera)
            reportesPdfService.addCellTb(tablaMateriales, new Paragraph("CANTIDAD", times7bold), celdaCabecera)
            reportesPdfService.addCellTb(tablaMateriales, new Paragraph("UNITARIO(\$)", times7bold), celdaCabecera)
            reportesPdfService.addCellTb(tablaMateriales, new Paragraph("C.TOTAL(\$)", times7bold), celdaCabecera)

            res.eachWithIndex { r, i ->
                if (r["grpocdgo"] == 1) {
                    bandMat = 1
                    reportesPdfService.addCellTb(tablaMateriales, new Paragraph(r["itemcdgo"], times8normal), prmsFilaIzquierda)
                    reportesPdfService.addCellTb(tablaMateriales, new Paragraph(r["itemnmbr"], times8normal), prmsFilaIzquierda)
                    reportesPdfService.addCellTb(tablaMateriales, new Paragraph(r["unddcdgo"], times8normal), prmsFila)
                    reportesPdfService.addCellTb(tablaMateriales, new Paragraph(numero(r["rbrocntd"], 5)?.toString(), times8normal), prmsFila)
                    if (params.desglose != '0') {
                        reportesPdfService.addCellTb(tablaMateriales, new Paragraph(numero(r["rbpcpcun"], 5)?.toString(), times8normal), prmsFilaDerecha)
                        reportesPdfService.addCellTb(tablaMateriales, new Paragraph(numero(r["parcial"], 5)?.toString(), times8normal), prmsFilaDerecha)
                        totalMat += r["parcial"]
                    }else{
                        reportesPdfService.addCellTb(tablaMateriales, new Paragraph(numero((r["rbpcpcun"] + r["parcial_t"] / r["rbrocntd"]), 5)?.toString(), times8normal), prmsFilaDerecha)
                        reportesPdfService.addCellTb(tablaMateriales, new Paragraph(numero((r["parcial"] + r["parcial_t"]), 5)?.toString(), times8normal), prmsFilaDerecha)
                        totalMat += (r["parcial"] + r["parcial_t"])
                    }
                }
            }

            reportesPdfService.addCellTb(tablaMateriales, new Paragraph("", times8bold), [border: Color.WHITE, colspan: 4])
            reportesPdfService.addCellTb(tablaMateriales, new Paragraph("TOTAL", times8bold), prmsFilaDerecha)
            reportesPdfService.addCellTb(tablaMateriales, new Paragraph(numero(totalMat, 5)?.toString(), times8bold), prmsFilaDerecha)
            reportesPdfService.addCellTb(tablaMateriales, new Paragraph("", times8bold), [border: Color.WHITE, colspan: 6])

            //TRANSPORTE
            PdfPTable tablaTransporte = new PdfPTable(8);
            tablaTransporte.setWidthPercentage(100);
            tablaTransporte.setWidths(arregloEnteros([11,25,8,11,11,12,10,10]))

            reportesPdfService.addCellTb(tablaTransporte, new Paragraph("TRANSPORTE", times12bold), tituloRubro)

            reportesPdfService.addCellTb(tablaTransporte, new Paragraph("CÓDIGO", times7bold), celdaCabecera)
            reportesPdfService.addCellTb(tablaTransporte, new Paragraph("DESCRIPCIÓN", times7bold), celdaCabecera)
            reportesPdfService.addCellTb(tablaTransporte, new Paragraph("UNIDAD", times7bold), celdaCabecera)
            reportesPdfService.addCellTb(tablaTransporte, new Paragraph("PES/VOL", times7bold), celdaCabecera)
            reportesPdfService.addCellTb(tablaTransporte, new Paragraph("CANTIDAD", times7bold), celdaCabecera)
            reportesPdfService.addCellTb(tablaTransporte, new Paragraph("DISTANCIA", times7bold), celdaCabecera)
            reportesPdfService.addCellTb(tablaTransporte, new Paragraph("TARIFA", times7bold), celdaCabecera)
            reportesPdfService.addCellTb(tablaTransporte, new Paragraph("C.TOTAL(\$)", times7bold), celdaCabecera)

            res.eachWithIndex { r, i ->
                if (r["grpocdgo"]== 1 && params.desglose != '0') {
                    reportesPdfService.addCellTb(tablaTransporte, new Paragraph(r["itemcdgo"], times8normal), prmsFilaIzquierda)
                    reportesPdfService.addCellTb(tablaTransporte, new Paragraph(r["itemnmbr"], times8normal), prmsFilaIzquierda)
                    if(r["tplscdgo"].trim() =='P' || r["tplscdgo"].trim() =='P1' ){
                        reportesPdfService.addCellTb(tablaTransporte, new Paragraph("ton-km", times8normal), prmsFila)
                    }else{
                        if(r["tplscdgo"].trim() =='V' || r["tplscdgo"].trim() =='V1' || r["tplscdgo"].trim() =='V2') {
                            reportesPdfService.addCellTb(tablaTransporte, new Paragraph("m3-km", times8normal), prmsFila)
                        }else{
                            reportesPdfService.addCellTb(tablaTransporte, new Paragraph(r["unddcdgo"], times8normal), prmsFila)
                        }
                    }
                    reportesPdfService.addCellTb(tablaTransporte, new Paragraph(numero(r["itempeso"], 5)?.toString(), times8normal), prmsFila)
                    reportesPdfService.addCellTb(tablaTransporte, new Paragraph(numero(r["rbrocntd"], 5)?.toString(), times8normal), prmsFila)
                    reportesPdfService.addCellTb(tablaTransporte, new Paragraph(numero(r["distancia"], 5)?.toString(), times8normal), prmsFila)
                    reportesPdfService.addCellTb(tablaTransporte, new Paragraph(numero(r["tarifa"], 5)?.toString(), times8normal), prmsFila)
                    reportesPdfService.addCellTb(tablaTransporte, new Paragraph(numero(r["parcial_t"], 5)?.toString(), times8normal), prmsFilaDerecha)
                    total += r["parcial_t"]
                }
            }

            reportesPdfService.addCellTb(tablaTransporte, new Paragraph("", times8bold), [border: Color.WHITE, colspan: 6])
            reportesPdfService.addCellTb(tablaTransporte, new Paragraph("TOTAL", times8bold), prmsFila)
            reportesPdfService.addCellTb(tablaTransporte, new Paragraph(numero(total, 5)?.toString(), times8bold), prmsFilaDerecha)
            reportesPdfService.addCellTb(tablaTransporte, new Paragraph("", times8bold), [border: Color.WHITE, colspan: 6])

            //COSTOS INDIRECTOS
            def totalRubro

//            if (params.desglose == '1') {
            totalRubro = total + totalHer + totalMan + totalMat
//            } else {
//                totalRubro = totalHer + totalMan + totalMat
//            }

            def totalIndi = totalRubro?.toDouble() * indi / 100

            PdfPTable tablaIndirectos = new PdfPTable(3);
            tablaIndirectos.setWidthPercentage(70);
            tablaIndirectos.setWidths(arregloEnteros([50,25,25]))
            tablaIndirectos.horizontalAlignment = Element.ALIGN_LEFT;

            reportesPdfService.addCellTb(tablaIndirectos, new Paragraph("COSTOS INDIRECTOS", times12bold), tituloRubro)

            reportesPdfService.addCellTb(tablaIndirectos, new Paragraph("DESCRIPCIÓN", times7bold), celdaCabecera)
            reportesPdfService.addCellTb(tablaIndirectos, new Paragraph("PORCENTAJE", times7bold), celdaCabecera)
            reportesPdfService.addCellTb(tablaIndirectos, new Paragraph("VALOR", times7bold), celdaCabecera)

            reportesPdfService.addCellTb(tablaIndirectos, new Paragraph("COSTOS INDIRECTOS", times8normal), prmsFilaIzquierda)
            reportesPdfService.addCellTb(tablaIndirectos, new Paragraph(numero(indi, 1)?.toString() + "%", times8normal), prmsFila)
            reportesPdfService.addCellTb(tablaIndirectos, new Paragraph(numero(totalIndi, 5)?.toString(), times8normal), prmsFila)
            reportesPdfService.addCellTb(tablaIndirectos, new Paragraph("", times8bold), [border: Color.WHITE, colspan: 3])

            PdfPTable tablaTotales = new PdfPTable(2);
            tablaTotales.setWidthPercentage(40);
            tablaTotales.setWidths(arregloEnteros([50,25]))
            tablaTotales.horizontalAlignment = Element.ALIGN_RIGHT;

            reportesPdfService.addCellTb(tablaTotales, new Paragraph("COSTO UNITARIO DIRECTO", times8bold), celdaCabeceraIzquierda)
            reportesPdfService.addCellTb(tablaTotales, new Paragraph(numero(totalRubro, 2)?.toString(), times8bold), celdaCabeceraDerecha)

            reportesPdfService.addCellTb(tablaTotales, new Paragraph("COSTOS INDIRECTO", times8bold), prmsFilaIzquierda)
            reportesPdfService.addCellTb(tablaTotales, new Paragraph(numero(totalIndi, 2)?.toString(), times8bold), prmsFilaDerecha)

            reportesPdfService.addCellTb(tablaTotales, new Paragraph("COSTO TOTAL DEL RUBRO", times8bold), prmsFilaIzquierda)
            reportesPdfService.addCellTb(tablaTotales, new Paragraph(numero((totalRubro + totalIndi), 2)?.toString(), times8bold), prmsFilaDerecha)

            reportesPdfService.addCellTb(tablaTotales, new Paragraph("PRECIO UNITARIO \$USD", times8bold), celdaCabeceraIzquierda2)
            reportesPdfService.addCellTb(tablaTotales, new Paragraph(numero((totalRubro + totalIndi), 2)?.toString(), times8bold), celdaCabeceraDerecha2)

            PdfPTable tablaNota = new PdfPTable(2);
            tablaNota.setWidthPercentage(100);
            tablaNota.setWidths(arregloEnteros([6, 94]))
            if(rubro?.codigo?.split('-')[0] == 'TR'){
                reportesPdfService.addCellTb(tablaNota, new Paragraph("Distancia a la escombrera:", times8bold), prmsFilaIzquierda)
                reportesPdfService.addCellTb(tablaNota, new Paragraph("${obra?.distanciaDesalojo}" + "km", times8normal), prmsFilaIzquierda)
            }
            reportesPdfService.addCellTb(tablaNota, new Paragraph("Nota:", times8bold), prmsFilaIzquierda)
            reportesPdfService.addCellTb(tablaNota, new Paragraph("Los cálculos se hacen con todos los " +
                    "decimales y el resultado final se lo redondea a dos decimales.", times8normal), prmsFilaIzquierda)

            document.add(headers)
            document.add(tablaCoeficiente)
            document.add(tablaEquipos)
            document.add(tablaManoObra)
            document.add(tablaMateriales)
            if(params.desglose != '0'){
                document.add(tablaTransporte)
            }
            document.add(tablaIndirectos)
            document.add(tablaTotales)
            document.add(tablaNota)
        }

        document.close();
        pdfw.close()
        byte[] b = baos.toByteArray();
        response.setContentType("application/pdf")
        response.setHeader("Content-disposition", "attachment; filename=" + name)
        response.setContentLength(b.length)
        response.getOutputStream().write(b)
    }

    def reporteRubrosVaeRegistro(){
        println("params rrvr " + params)

        def obra = Obra.get(params.obra)
        def fecha1
        def fecha2
        def rubros = []
        def lugar = obra?.lugar
        def indi = obra?.totales

        if(obra?.fechaPreciosRubros) {
            fecha1 = obra?.fechaPreciosRubros
        }

        if(obra?.fechaOficioSalida) {
            fecha2 = obra?.fechaOficioSalida
        }

        if(obra.estado != 'R') {
            println "antes de imprimir rubros.. actualiza desalojo y herramienta menor"
            preciosService.ac_transporteDesalojo(obra.id)
            preciosService.ac_rbroObra(obra.id)
        }

        rubros = VolumenesObra.findAllByObra(obra, [sort: "orden"]).item.unique()

        def bandMat = 0
        def band = 0
        def bandTrans = params.desglose

        try {
            indi = indi.toDouble()
        } catch (e) {
            println "error parse " + e
            indi = 21.5
        }

        def prmsHeaderHoja = [border: Color.WHITE]
        def prmsFila = [border: Color.WHITE, align : Element.ALIGN_CENTER, valign: Element.ALIGN_MIDDLE]
        def prmsFilaIzquierda = [border: Color.WHITE, align : Element.ALIGN_LEFT, valign: Element.ALIGN_MIDDLE]
        def prmsFilaDerecha = [border: Color.WHITE, align : Element.ALIGN_RIGHT, valign: Element.ALIGN_MIDDLE]
        def prmsHeaderHoja2 = [border: Color.WHITE, colspan: 9]
        def prmsHeader = [border: Color.WHITE, colspan: 7, bg: new Color(73, 175, 205),
                          align : Element.ALIGN_CENTER, valign: Element.ALIGN_MIDDLE]
        def prmsHeader2 = [border: Color.WHITE, colspan: 3, bg: new Color(73, 175, 205),
                           align : Element.ALIGN_CENTER, valign: Element.ALIGN_MIDDLE]
        def prmsCellHead = [border: Color.WHITE, bg: new Color(73, 175, 205),
                            align : Element.ALIGN_CENTER, valign: Element.ALIGN_MIDDLE]
        def prmsCellCenter = [border: Color.BLACK, align: Element.ALIGN_CENTER, valign: Element.ALIGN_MIDDLE]
        def prmsCellRight = [border: Color.BLACK, align: Element.ALIGN_RIGHT, valign: Element.ALIGN_MIDDLE]
        def prmsCellLeft = [border: Color.BLACK, valign: Element.ALIGN_MIDDLE]
        def prmsSubtotal = [border: Color.BLACK, colspan: 6,
                            align : Element.ALIGN_RIGHT, valign: Element.ALIGN_MIDDLE]
        def prmsNum = [border: Color.BLACK, align: Element.ALIGN_RIGHT, valign: Element.ALIGN_MIDDLE]

        def celdaCabecera = [border: Color.BLACK, bg: new Color(220, 220, 220), align: Element.ALIGN_CENTER, valign: Element.ALIGN_MIDDLE, bordeBot: "1"]
        def celdaCabeceraIzquierda = [bct: Color.BLACK, bcl: Color.WHITE, bcr:Color.WHITE, bcb: Color.WHITE, align: Element.ALIGN_LEFT, valign: Element.ALIGN_MIDDLE]
        def celdaCabeceraDerecha = [bct: Color.BLACK, bcl: Color.WHITE, bcr:Color.WHITE, bcb: Color.WHITE, align: Element.ALIGN_RIGHT, valign: Element.ALIGN_MIDDLE]
        def celdaCabeceraCentro = [bct: Color.BLACK, bcl: Color.WHITE, bcr:Color.WHITE, bcb: Color.WHITE, align: Element.ALIGN_CENTER, valign: Element.ALIGN_MIDDLE]
        def celdaCabeceraCentro2 = [bcb: Color.BLACK, bcl: Color.WHITE, bcr:Color.WHITE, bct: Color.WHITE, align: Element.ALIGN_CENTER, valign: Element.ALIGN_MIDDLE]
        def celdaCabeceraDerecha2 = [bcb: Color.BLACK, bcl: Color.WHITE, bcr:Color.WHITE, bct: Color.WHITE, align: Element.ALIGN_RIGHT, valign: Element.ALIGN_MIDDLE]
        def celdaCabeceraIzquierda2 = [bcb: Color.BLACK, bcl: Color.WHITE, bcr:Color.WHITE, bct: Color.WHITE, align: Element.ALIGN_LEFT, valign: Element.ALIGN_MIDDLE]

        def tituloRubro = [height: 20, border: Color.WHITE, colspan: 12, align : Element.ALIGN_LEFT, valign: Element.ALIGN_TOP]
        def tituloRubro13 = [height: 20, border: Color.WHITE, colspan: 13, align : Element.ALIGN_LEFT, valign: Element.ALIGN_TOP]
        def tituloRubro3 = [height: 20, border: Color.WHITE, colspan: 3, align : Element.ALIGN_LEFT, valign: Element.ALIGN_TOP]

        def prms = [prmsHeaderHoja: prmsHeaderHoja, prmsHeader: prmsHeader, prmsHeader2: prmsHeader2,
                    prmsCellHead: prmsCellHead, prmsCell: prmsCellCenter, prmsCellLeft: prmsCellLeft, prmsSubtotal: prmsSubtotal, prmsNum: prmsNum, prmsHeaderHoja2: prmsHeaderHoja2, prmsCellRight: prmsCellRight]

        Font times12bold = new Font(Font.TIMES_ROMAN, 12, Font.BOLD)
        Font times14bold = new Font(Font.TIMES_ROMAN, 14, Font.BOLD)
        Font times10bold = new Font(Font.TIMES_ROMAN, 10, Font.BOLD)
        Font times10normal = new Font(Font.TIMES_ROMAN, 10, Font.NORMAL)
        Font times8bold = new Font(Font.TIMES_ROMAN, 8, Font.BOLD)
        Font times8normal = new Font(Font.TIMES_ROMAN, 8, Font.NORMAL)
        Font times7bold = new Font(Font.TIMES_ROMAN, 7, Font.BOLD)
        Font times7normal = new Font(Font.TIMES_ROMAN, 7, Font.NORMAL)
        Font times10boldWhite = new Font(Font.TIMES_ROMAN, 10, Font.BOLD);
        Font times8boldWhite = new Font(Font.TIMES_ROMAN, 8, Font.BOLD)
        times8boldWhite.setColor(Color.WHITE)
        times10boldWhite.setColor(Color.WHITE)

        def baos = new ByteArrayOutputStream()
        def name = "reporteRubrosVae_" + new Date().format("ddMMyyyy_hhmm") + ".pdf";

        Document document
        document = new Document(PageSize.A4.rotate())
        document.setMargins(60, 24, 45, 45);

        def pdfw = PdfWriter.getInstance(document, baos);
        document.open();
        document.addTitle("Rubros " + new Date().format("dd_MM_yyyy"));
        document.addSubject("Generado por el sistema Obras");
        document.addKeywords("documentosObra, janus, rubros");
        document.addAuthor("OBRAS");
        document.addCreator("Tedein SA");

        rubros.eachWithIndex{ rubro, indice->

            document.newPage();

            def nombre = rubro?.nombre

            preciosService.ac_rbroObra(obra.id)
            def res = preciosService.precioUnitarioVolumenObraAsc("*", obra.id, rubro.id)
            def vae = preciosService.vae_rb(obra.id,rubro.id)
            def total = 0, totalHer = 0, totalMan = 0, totalMat = 0, totalHerRel = 0,
                totalHerVae = 0, totalManRel = 0, totalManVae = 0, totalMatRel = 0, totalMatVae = 0,
                totalTRel=0, totalTVae=0

            Paragraph headers = new Paragraph();
            addEmptyLine(headers, 1);
            headers.setAlignment(Element.ALIGN_CENTER);
            headers.add(new Paragraph("G.A.D. LOS RÍOS", times14bold));
            headers.add(new Paragraph("COORDINACIÓN DE FIJACIÓN DE PRECIOS UNITARIOS - ANÁLISIS DE PRECIOS UNITARIOS", times10bold));
            headers.add(new Paragraph("", times14bold));

            PdfPTable tablaCoeficiente = new PdfPTable(6);
            tablaCoeficiente.setWidthPercentage(100);
            tablaCoeficiente.setWidths(arregloEnteros([15,18, 15,18, 15,18]))

            reportesPdfService.addCellTb(tablaCoeficiente, new Paragraph("Fecha: ", times10bold), prmsHeaderHoja)
            reportesPdfService.addCellTb(tablaCoeficiente, new Paragraph((fecha2?.format("dd-MM-yyyy") ?: ''), times10normal), [border: Color.WHITE, colspan: 3])
            reportesPdfService.addCellTb(tablaCoeficiente, new Paragraph("Fecha Act. P.U: ", times10bold), prmsHeaderHoja)
            reportesPdfService.addCellTb(tablaCoeficiente, new Paragraph((fecha1?.format("dd-MM-yyyy") ?: '') , times10normal), prmsHeaderHoja)

            reportesPdfService.addCellTb(tablaCoeficiente, new Paragraph("Código de obra: ", times10bold), prmsHeaderHoja)
            reportesPdfService.addCellTb(tablaCoeficiente, new Paragraph((obra?.codigo ?: ''), times10normal), [border: Color.WHITE, colspan: 5])

            reportesPdfService.addCellTb(tablaCoeficiente, new Paragraph("Presupuesto: ", times10bold), prmsHeaderHoja)
            reportesPdfService.addCellTb(tablaCoeficiente, new Paragraph((obra?.nombre ?: ''), times10normal), [border: Color.WHITE, colspan: 5])

            reportesPdfService.addCellTb(tablaCoeficiente, new Paragraph("Código de rubro: ", times10bold), prmsHeaderHoja)
            reportesPdfService.addCellTb(tablaCoeficiente, new Paragraph((rubro?.codigo ?: ''), times10normal), prmsHeaderHoja)
            reportesPdfService.addCellTb(tablaCoeficiente, new Paragraph("Código de especificación: ", times10bold), prmsHeaderHoja)
            reportesPdfService.addCellTb(tablaCoeficiente, new Paragraph((rubro?.codigoEspecificacion ?: ''), times10normal), prmsHeaderHoja)
            reportesPdfService.addCellTb(tablaCoeficiente, new Paragraph("Unidad: ", times10bold), prmsHeaderHoja)
            reportesPdfService.addCellTb(tablaCoeficiente, new Paragraph((rubro?.unidad?.codigo ?: ''), times10normal), prmsHeaderHoja)

            reportesPdfService.addCellTb(tablaCoeficiente, new Paragraph("Descripción: ", times10bold), prmsHeaderHoja)
            reportesPdfService.addCellTb(tablaCoeficiente, new Paragraph((nombre ?: ''), times10normal), [border: Color.WHITE, colspan: 5])

            //EQUIPOS
            PdfPTable tablaEquipos = new PdfPTable(12);
            tablaEquipos.setWidthPercentage(100);
            tablaEquipos.setWidths(arregloEnteros([8,30,7,6,6,9,7,7,7,5,4,8]))

            reportesPdfService.addCellTb(tablaEquipos, new Paragraph("EQUIPOS", times12bold), tituloRubro)

            reportesPdfService.addCellTb(tablaEquipos, new Paragraph("CÓDIGO", times7bold), celdaCabecera)
            reportesPdfService.addCellTb(tablaEquipos, new Paragraph("DESCRIPCIÓN", times7bold), celdaCabecera)
            reportesPdfService.addCellTb(tablaEquipos, new Paragraph("CANTIDAD", times7bold), celdaCabecera)
            reportesPdfService.addCellTb(tablaEquipos, new Paragraph("TARIFA (\$/H)", times7bold), celdaCabecera)
            reportesPdfService.addCellTb(tablaEquipos, new Paragraph("COSTOS (\$)", times7bold), celdaCabecera)
            reportesPdfService.addCellTb(tablaEquipos, new Paragraph("RENDIMIENTO", times7bold), celdaCabecera)
            reportesPdfService.addCellTb(tablaEquipos, new Paragraph("C.TOTAL(\$)", times7bold), celdaCabecera)
            reportesPdfService.addCellTb(tablaEquipos, new Paragraph("PESO RELAT(%)", times7bold), celdaCabecera)
            reportesPdfService.addCellTb(tablaEquipos, new Paragraph("CPC", times7bold), celdaCabecera)
            reportesPdfService.addCellTb(tablaEquipos, new Paragraph("NP/EP/ ND", times7bold), celdaCabecera)
            reportesPdfService.addCellTb(tablaEquipos, new Paragraph("VAE (%)", times7bold), celdaCabecera)
            reportesPdfService.addCellTb(tablaEquipos, new Paragraph("VAE(%) ELEMENTO", times7bold), celdaCabecera)

            vae.eachWithIndex { r, i ->
                if (r["grpocdgo"] == 3) {
                    reportesPdfService.addCellTb(tablaEquipos, new Paragraph(r["itemcdgo"], times8normal), prmsFilaIzquierda)
                    reportesPdfService.addCellTb(tablaEquipos, new Paragraph(r["itemnmbr"], times8normal), prmsFilaIzquierda)
                    reportesPdfService.addCellTb(tablaEquipos, new Paragraph(numero(r["rbrocntd"], 5)?.toString(), times8normal), prmsFila)
                    reportesPdfService.addCellTb(tablaEquipos, new Paragraph(numero(r["rbpcpcun"], 5)?.toString(), times8normal), prmsFilaDerecha)
                    reportesPdfService.addCellTb(tablaEquipos, new Paragraph((numero((r["rbpcpcun"] * r["rbrocntd"]), 5))?.toString(), times8normal), prmsFilaDerecha)
                    reportesPdfService.addCellTb(tablaEquipos, new Paragraph(numero(r["rndm"], 5)?.toString(), times8normal), prmsFila)
                    reportesPdfService.addCellTb(tablaEquipos, new Paragraph(numero(r["parcial"], 5)?.toString(), times8normal), prmsFilaDerecha)
                    reportesPdfService.addCellTb(tablaEquipos, new Paragraph(numero(r["relativo"], 2)?.toString(), times8normal), prmsFila)
                    reportesPdfService.addCellTb(tablaEquipos, new Paragraph(r["itemcpac"]?.toString(), times8normal), prmsFila)
                    reportesPdfService.addCellTb(tablaEquipos, new Paragraph(r["tpbncdgo"], times8normal), prmsFila)
                    reportesPdfService.addCellTb(tablaEquipos, new Paragraph(numero(r["vae"], 2)?.toString(), times8normal), prmsFila)
                    reportesPdfService.addCellTb(tablaEquipos, new Paragraph((numero(r["vae_vlor"],2))?.toString(), times8normal), prmsFila)
                    totalHer += r["parcial"]
                    totalHerRel += r["relativo"]
                    totalHerVae += r["vae_vlor"]
                }
            }

            reportesPdfService.addCellTb(tablaEquipos, new Paragraph("", times8bold), [border: Color.WHITE, colspan: 5])
            reportesPdfService.addCellTb(tablaEquipos, new Paragraph("TOTAL", times8bold), prmsFila)
            reportesPdfService.addCellTb(tablaEquipos, new Paragraph(numero(totalHer, 5)?.toString(), times8bold), prmsFilaDerecha)
            reportesPdfService.addCellTb(tablaEquipos, new Paragraph(numero(totalHerRel, 2)?.toString(), times8bold), prmsFila)
            reportesPdfService.addCellTb(tablaEquipos, new Paragraph("", times8bold), [border: Color.WHITE, colspan: 3])
            reportesPdfService.addCellTb(tablaEquipos, new Paragraph(numero(totalHerVae, 2)?.toString(), times8bold), prmsFila)

            //MANO DE OBRA
            PdfPTable tablaManoObra = new PdfPTable(12);
            tablaManoObra.setWidthPercentage(100);
            tablaManoObra.setWidths(arregloEnteros([6,32,7,6,6,9,7,7,7,5,4,8]))

            reportesPdfService.addCellTb(tablaManoObra, new Paragraph("MANO DE OBRA", times12bold), tituloRubro)

            reportesPdfService.addCellTb(tablaManoObra, new Paragraph("CÓDIGO", times7bold), celdaCabecera)
            reportesPdfService.addCellTb(tablaManoObra, new Paragraph("DESCRIPCIÓN", times7bold), celdaCabecera)
            reportesPdfService.addCellTb(tablaManoObra, new Paragraph("CANTIDAD", times7bold), celdaCabecera)
            reportesPdfService.addCellTb(tablaManoObra, new Paragraph("JORNAL (\$/H)", times7bold), celdaCabecera)
            reportesPdfService.addCellTb(tablaManoObra, new Paragraph("COSTOS (\$)", times7bold), celdaCabecera)
            reportesPdfService.addCellTb(tablaManoObra, new Paragraph("RENDIMIENTO", times7bold), celdaCabecera)
            reportesPdfService.addCellTb(tablaManoObra, new Paragraph("C.TOTAL (\$)", times7bold), celdaCabecera)
            reportesPdfService.addCellTb(tablaManoObra, new Paragraph("PESO RELAT(%)", times7bold), celdaCabecera)
            reportesPdfService.addCellTb(tablaManoObra, new Paragraph("CPC", times7bold), celdaCabecera)
            reportesPdfService.addCellTb(tablaManoObra, new Paragraph("NP/EP/ ND", times7bold), celdaCabecera)
            reportesPdfService.addCellTb(tablaManoObra, new Paragraph("VAE (%)", times7bold), celdaCabecera)
            reportesPdfService.addCellTb(tablaManoObra, new Paragraph("VAE(%) ELEMENTO", times7bold), celdaCabecera)

            vae.eachWithIndex { r, i ->
                if (r["grpocdgo"] == 2) {
                    reportesPdfService.addCellTb(tablaManoObra, new Paragraph(r["itemcdgo"], times8normal), prmsFilaIzquierda)
                    reportesPdfService.addCellTb(tablaManoObra, new Paragraph(r["itemnmbr"], times8normal), prmsFilaIzquierda)
                    reportesPdfService.addCellTb(tablaManoObra, new Paragraph(numero(r["rbrocntd"], 5)?.toString(), times8normal), prmsFila)
                    reportesPdfService.addCellTb(tablaManoObra, new Paragraph(numero(r["rbpcpcun"], 5)?.toString(), times8normal), prmsFilaDerecha)
                    reportesPdfService.addCellTb(tablaManoObra, new Paragraph((numero((r["rbpcpcun"] * r["rbrocntd"]), 5))?.toString(), times8normal), prmsFilaDerecha)
                    reportesPdfService.addCellTb(tablaManoObra, new Paragraph(numero(r["rndm"], 5)?.toString(), times8normal), prmsFila)
                    reportesPdfService.addCellTb(tablaManoObra, new Paragraph(numero(r["parcial"], 5)?.toString(), times8normal), prmsFilaDerecha)
                    reportesPdfService.addCellTb(tablaManoObra, new Paragraph(numero(r["relativo"], 2)?.toString(), times8normal), prmsFila)
                    reportesPdfService.addCellTb(tablaManoObra, new Paragraph(r["itemcpac"]?.toString(), times8normal), prmsFila)
                    reportesPdfService.addCellTb(tablaManoObra, new Paragraph(r["tpbncdgo"], times8normal), prmsFila)
                    reportesPdfService.addCellTb(tablaManoObra, new Paragraph(numero(r["vae"], 2)?.toString(), times8normal), prmsFila)
                    reportesPdfService.addCellTb(tablaManoObra, new Paragraph((numero(r["vae_vlor"],2))?.toString(), times8normal), prmsFila)
                    totalMan += r["parcial"]
                    totalManRel += r["relativo"]
                    totalManVae += r["vae_vlor"]
                }
            }

            reportesPdfService.addCellTb(tablaManoObra, new Paragraph("", times8bold), [border: Color.WHITE, colspan: 5])
            reportesPdfService.addCellTb(tablaManoObra, new Paragraph("TOTAL", times8bold), prmsFila)
            reportesPdfService.addCellTb(tablaManoObra, new Paragraph(numero(totalMan, 5)?.toString(), times8bold), prmsFilaDerecha)
            reportesPdfService.addCellTb(tablaManoObra, new Paragraph(numero(totalManRel, 2)?.toString(), times8bold), prmsFila)
            reportesPdfService.addCellTb(tablaManoObra, new Paragraph("", times14bold), [border: Color.WHITE, colspan: 3])
            reportesPdfService.addCellTb(tablaManoObra, new Paragraph(numero(totalManVae, 2)?.toString(), times8bold), prmsFila)


            //MATERIALES
            PdfPTable tablaMateriales = new PdfPTable(11);
            tablaMateriales.setWidthPercentage(100);
            tablaMateriales.setWidths(arregloEnteros([8,37, 6,6,9,7,7,7,5,4,8]))

            if(params.desglose == '0'){
                reportesPdfService.addCellTb(tablaMateriales, new Paragraph("MATERIALES INCLUIDO TRANSPORTE", times12bold), tituloRubro)
            }else{
                reportesPdfService.addCellTb(tablaMateriales, new Paragraph("MATERIALES", times12bold), tituloRubro)
            }

            reportesPdfService.addCellTb(tablaMateriales, new Paragraph("CÓDIGO", times7bold), celdaCabecera)
            reportesPdfService.addCellTb(tablaMateriales, new Paragraph("DESCRIPCIÓN", times7bold), celdaCabecera)
            reportesPdfService.addCellTb(tablaMateriales, new Paragraph("UNIDAD", times7bold), celdaCabecera)
            reportesPdfService.addCellTb(tablaMateriales, new Paragraph("CANTI- DAD", times7bold), celdaCabecera)
            reportesPdfService.addCellTb(tablaMateriales, new Paragraph("UNITARIO(\$)", times7bold), celdaCabecera)
            reportesPdfService.addCellTb(tablaMateriales, new Paragraph("C.TOTAL(\$)", times7bold), celdaCabecera)
            reportesPdfService.addCellTb(tablaMateriales, new Paragraph("PESO RELAT(%)", times7bold), celdaCabecera)
            reportesPdfService.addCellTb(tablaMateriales, new Paragraph("CPC", times7bold), celdaCabecera)
            reportesPdfService.addCellTb(tablaMateriales, new Paragraph("NP/EP/ND", times7bold), celdaCabecera)
            reportesPdfService.addCellTb(tablaMateriales, new Paragraph("VAE(%)", times7bold), celdaCabecera)
            reportesPdfService.addCellTb(tablaMateriales, new Paragraph("VAE(%) ELEMENTO", times7bold), celdaCabecera)

            vae.eachWithIndex { r, i ->
                if (r["grpocdgo"] == 1) {
                    bandMat = 1
                    reportesPdfService.addCellTb(tablaMateriales, new Paragraph(r["itemcdgo"], times8normal), prmsFilaIzquierda)
                    reportesPdfService.addCellTb(tablaMateriales, new Paragraph(r["itemnmbr"], times8normal), prmsFilaIzquierda)
                    reportesPdfService.addCellTb(tablaMateriales, new Paragraph(r["unddcdgo"], times8normal), prmsFila)
                    reportesPdfService.addCellTb(tablaMateriales, new Paragraph(numero(r["rbrocntd"], 5)?.toString(), times8normal), prmsFila)
                    if (params.desglose != '0') {
                        reportesPdfService.addCellTb(tablaMateriales, new Paragraph(numero(r["rbpcpcun"], 5)?.toString(), times8normal), prmsFilaDerecha)
                        reportesPdfService.addCellTb(tablaMateriales, new Paragraph(numero(r["parcial"], 5)?.toString(), times8normal), prmsFilaDerecha)
                        reportesPdfService.addCellTb(tablaMateriales, new Paragraph(numero(r["relativo"], 2)?.toString(), times8normal), prmsFila)
                        reportesPdfService.addCellTb(tablaMateriales, new Paragraph(r["itemcpac"]?.toString(), times8normal), prmsFila)
                        reportesPdfService.addCellTb(tablaMateriales, new Paragraph(r["tpbncdgo"], times8normal), prmsFila)
                        reportesPdfService.addCellTb(tablaMateriales, new Paragraph(numero(r["vae"], 2)?.toString(), times8normal), prmsFila)
                        reportesPdfService.addCellTb(tablaMateriales, new Paragraph((numero(r["vae_vlor"],2))?.toString(), times8normal), prmsFila)

                        totalMat += r["parcial"]
                        totalMatRel += r["relativo"]
                        totalMatVae += r["vae_vlor"]
                    }else{
                        reportesPdfService.addCellTb(tablaMateriales, new Paragraph(numero((r["rbpcpcun"] + r["parcial_t"] / r["rbrocntd"]), 5)?.toString(), times8normal), prmsFilaDerecha)
                        reportesPdfService.addCellTb(tablaMateriales, new Paragraph(numero((r["parcial"] + r["parcial_t"]), 5)?.toString(), times8normal), prmsFilaDerecha)
                        reportesPdfService.addCellTb(tablaMateriales, new Paragraph(numero((r["relativo"] + r["relativo_t"]), 2)?.toString(), times8normal), prmsFila)
                        reportesPdfService.addCellTb(tablaMateriales, new Paragraph(r["itemcpac"]?.toString(), times8normal), prmsFila)
                        reportesPdfService.addCellTb(tablaMateriales, new Paragraph(r["tpbncdgo"], times8normal), prmsFila)
                        reportesPdfService.addCellTb(tablaMateriales, new Paragraph(numero(r["vae"], 2)?.toString(), times8normal), prmsFila)
                        reportesPdfService.addCellTb(tablaMateriales, new Paragraph((numero(r["vae_vlor"] + r["vae_vlor_t"],2))?.toString(), times8normal), prmsFila)

                        totalMat += (r["parcial"] + r["parcial_t"])
                        totalMatRel += (r["relativo"] + r["relativo_t"])
                        totalMatVae += (r["vae_vlor"] + r["vae_vlor_t"])
                    }

                }
            }

            reportesPdfService.addCellTb(tablaMateriales, new Paragraph("", times8bold), [border: Color.WHITE, colspan: 4])
            reportesPdfService.addCellTb(tablaMateriales, new Paragraph("TOTAL", times8bold), prmsFila)
            reportesPdfService.addCellTb(tablaMateriales, new Paragraph(numero(totalMat, 5)?.toString(), times8bold), prmsFilaDerecha)
            reportesPdfService.addCellTb(tablaMateriales, new Paragraph(numero(totalMatRel, 2)?.toString(), times8bold), prmsFila)
            reportesPdfService.addCellTb(tablaMateriales, new Paragraph("", times8bold), [border: Color.WHITE, colspan: 3])
            reportesPdfService.addCellTb(tablaMateriales, new Paragraph(numero(totalMatVae, 2)?.toString(), times8bold), prmsFila)

            //TRANSPORTE
            PdfPTable tablaTransporte = new PdfPTable(13);
            tablaTransporte.setWidthPercentage(100);
            tablaTransporte.setWidths(arregloEnteros([8,27,4,6,6,6,9,7,7,7,5,4,8]))

            reportesPdfService.addCellTb(tablaTransporte, new Paragraph("TRANSPORTE", times12bold), tituloRubro13)

            reportesPdfService.addCellTb(tablaTransporte, new Paragraph("CÓDIGO", times7bold), celdaCabecera)
            reportesPdfService.addCellTb(tablaTransporte, new Paragraph("DESCRIPCIÓN", times7bold), celdaCabecera)
            reportesPdfService.addCellTb(tablaTransporte, new Paragraph("UNI- DAD", times7bold), celdaCabecera)
            reportesPdfService.addCellTb(tablaTransporte, new Paragraph("PES/VOL", times7bold), celdaCabecera)
            reportesPdfService.addCellTb(tablaTransporte, new Paragraph("CANT.", times7bold), celdaCabecera)
            reportesPdfService.addCellTb(tablaTransporte, new Paragraph("DISTAN- CIA", times7bold), celdaCabecera)
            reportesPdfService.addCellTb(tablaTransporte, new Paragraph("TARIFA", times7bold), celdaCabecera)
            reportesPdfService.addCellTb(tablaTransporte, new Paragraph("C.TOTAL(\$)", times7bold), celdaCabecera)
            reportesPdfService.addCellTb(tablaTransporte, new Paragraph("PESO RELAT(%)", times7bold), celdaCabecera)
            reportesPdfService.addCellTb(tablaTransporte, new Paragraph("CPC", times7bold), celdaCabecera)
            reportesPdfService.addCellTb(tablaTransporte, new Paragraph("NP/EP/ ND", times7bold), celdaCabecera)
            reportesPdfService.addCellTb(tablaTransporte, new Paragraph("VAE(%)", times7bold), celdaCabecera)
            reportesPdfService.addCellTb(tablaTransporte, new Paragraph("VAE(%) ELEMENTO", times7bold), celdaCabecera)

            vae.eachWithIndex { r, i ->
                if (r["grpocdgo"]== 1 && params.desglose != '0') {
                    reportesPdfService.addCellTb(tablaTransporte, new Paragraph(r["itemcdgo"], times8normal), prmsFilaIzquierda)
                    reportesPdfService.addCellTb(tablaTransporte, new Paragraph(r["itemnmbr"], times8normal), prmsFilaIzquierda)
                    if(r["tplscdgo"].trim() =='P' || r["tplscdgo"].trim() =='P1' ){
                        reportesPdfService.addCellTb(tablaTransporte, new Paragraph("ton-km", times8normal), prmsFila)
                    }else{
                        if(r["tplscdgo"].trim() =='V' || r["tplscdgo"].trim() =='V1' || r["tplscdgo"].trim() =='V2') {
                            reportesPdfService.addCellTb(tablaTransporte, new Paragraph("m3-km", times8normal), prmsFila)
                        }else{
                            reportesPdfService.addCellTb(tablaTransporte, new Paragraph(r["unddcdgo"], times8normal), prmsFila)
                        }
                    }
                    reportesPdfService.addCellTb(tablaTransporte, new Paragraph(numero(r["itempeso"], 5)?.toString(), times8normal), prmsFila)
                    reportesPdfService.addCellTb(tablaTransporte, new Paragraph(numero(r["rbrocntd"], 5)?.toString(), times8normal), prmsFila)
                    reportesPdfService.addCellTb(tablaTransporte, new Paragraph(numero(r["distancia"], 5)?.toString(), times8normal), prmsFila)
                    reportesPdfService.addCellTb(tablaTransporte, new Paragraph(numero(r["tarifa"], 5)?.toString(), times8normal), prmsFila)
                    reportesPdfService.addCellTb(tablaTransporte, new Paragraph(numero(r["parcial_t"], 5)?.toString(), times8normal), prmsFilaDerecha)
                    reportesPdfService.addCellTb(tablaTransporte, new Paragraph(numero(r["relativo_t"], 2)?.toString(), times8normal), prmsFilaDerecha)
                    reportesPdfService.addCellTb(tablaTransporte, new Paragraph((r["itemcpac"] ?: '')?.toString(), times8normal), prmsFila)
                    reportesPdfService.addCellTb(tablaTransporte, new Paragraph(r["tpbncdgo"], times8normal), prmsFila)
                    reportesPdfService.addCellTb(tablaTransporte, new Paragraph(numero(r["vae_t"], 2)?.toString(), times8normal), prmsFila)
                    reportesPdfService.addCellTb(tablaTransporte, new Paragraph(numero(r["vae_vlor_t"], 2)?.toString(), times8normal), prmsFila)
                    total += r["parcial_t"]
                    totalTRel += r["relativo_t"]
                    totalTVae += r["vae_vlor_t"]
                }
            }

            reportesPdfService.addCellTb(tablaTransporte, new Paragraph("", times8bold), [border: Color.WHITE, colspan: 6])
            reportesPdfService.addCellTb(tablaTransporte, new Paragraph("TOTAL", times8bold), prmsFila)
            reportesPdfService.addCellTb(tablaTransporte, new Paragraph(numero(total, 5)?.toString(), times8bold), prmsFilaDerecha)
            reportesPdfService.addCellTb(tablaTransporte, new Paragraph(numero(totalTRel, 2)?.toString(), times8bold), prmsFilaDerecha)
            reportesPdfService.addCellTb(tablaTransporte, new Paragraph("", times14bold), [border: Color.WHITE, colspan: 3])
            reportesPdfService.addCellTb(tablaTransporte, new Paragraph(numero(totalTVae, 2)?.toString(), times8bold), prmsFila)


            //COSTOS INDIRECTOS
            def totalRubro = total + totalHer + totalMan + totalMat
            def totalRelativo = totalTRel + totalHerRel + totalMatRel + totalManRel
            def totalVae = totalTVae + totalHerVae + totalMatVae + totalManVae
            def totalIndi = totalRubro?.toDouble() * indi / 100

            PdfPTable tablaIndirectos = new PdfPTable(3);
            tablaIndirectos.setWidthPercentage(70);
            tablaIndirectos.setWidths(arregloEnteros([50,25,25]))
            tablaIndirectos.horizontalAlignment = Element.ALIGN_LEFT;

            reportesPdfService.addCellTb(tablaIndirectos, new Paragraph("COSTOS INDIRECTOS",  times12bold), tituloRubro3)

            reportesPdfService.addCellTb(tablaIndirectos, new Paragraph("DESCRIPCIÓN", times8bold), celdaCabecera)
            reportesPdfService.addCellTb(tablaIndirectos, new Paragraph("PORCENTAJE", times8bold), celdaCabecera)
            reportesPdfService.addCellTb(tablaIndirectos, new Paragraph("VALOR", times8bold), celdaCabecera)

            reportesPdfService.addCellTb(tablaIndirectos, new Paragraph("COSTOS INDIRECTOS", times8normal), prmsFilaIzquierda)
            reportesPdfService.addCellTb(tablaIndirectos, new Paragraph(numero(indi, 1)?.toString() + "%", times8normal), prmsFila)
            reportesPdfService.addCellTb(tablaIndirectos, new Paragraph(numero(totalIndi, 5)?.toString(), times8normal), prmsFila)

            reportesPdfService.addCellTb(tablaIndirectos, new Paragraph("", times8bold), [border: Color.WHITE, colspan: 3])
            reportesPdfService.addCellTb(tablaIndirectos, new Paragraph("", times8bold), [border: Color.WHITE, colspan: 3])

            PdfPTable tablaTotales = new PdfPTable(4);
            tablaTotales.setWidthPercentage(70);
            tablaTotales.setWidths(arregloEnteros([30,25,25,20]))
            tablaTotales.horizontalAlignment = Element.ALIGN_RIGHT;

            reportesPdfService.addCellTb(tablaTotales, new Paragraph("COSTO UNITARIO DIRECTO", times8bold), celdaCabeceraIzquierda)
            reportesPdfService.addCellTb(tablaTotales, new Paragraph(numero(totalRubro, 2)?.toString(), times8bold), celdaCabeceraDerecha)
            reportesPdfService.addCellTb(tablaTotales, new Paragraph(numero(totalRelativo, 2)?.toString(), times8bold), celdaCabeceraCentro)
            reportesPdfService.addCellTb(tablaTotales, new Paragraph(numero(totalVae, 2)?.toString(), times8bold), celdaCabeceraCentro)

            reportesPdfService.addCellTb(tablaTotales, new Paragraph("COSTOS INDIRECTO", times8bold), prmsFilaIzquierda)
            reportesPdfService.addCellTb(tablaTotales, new Paragraph(numero(totalIndi, 2)?.toString(), times8bold), prmsFilaDerecha)
            reportesPdfService.addCellTb(tablaTotales, new Paragraph("TOTAL", times8bold), prmsFila)
            reportesPdfService.addCellTb(tablaTotales, new Paragraph("TOTAL", times8bold), prmsFila)

            reportesPdfService.addCellTb(tablaTotales, new Paragraph("COSTO TOTAL DEL RUBRO", times8bold), prmsFilaIzquierda)
            reportesPdfService.addCellTb(tablaTotales, new Paragraph(numero((totalRubro + totalIndi), 2)?.toString(), times8bold), prmsFilaDerecha)
            reportesPdfService.addCellTb(tablaTotales, new Paragraph("PESO", times8bold), prmsFila)
            reportesPdfService.addCellTb(tablaTotales, new Paragraph("VAE", times8bold), prmsFila)

            reportesPdfService.addCellTb(tablaTotales, new Paragraph("PRECIO UNITARIO \$USD", times8bold), celdaCabeceraIzquierda2)
            reportesPdfService.addCellTb(tablaTotales, new Paragraph(numero((totalRubro + totalIndi), 2)?.toString(), times8bold), celdaCabeceraDerecha2)
            reportesPdfService.addCellTb(tablaTotales, new Paragraph("RELATIVO", times8bold), celdaCabeceraCentro2)
            reportesPdfService.addCellTb(tablaTotales, new Paragraph("(%)", times8bold), celdaCabeceraCentro2)

            PdfPTable tablaNota = new PdfPTable(2);
            tablaNota.setWidthPercentage(100);
            tablaNota.setWidths(arregloEnteros([6, 94]))
            if(rubro?.codigo?.split('-')[0] == 'TR'){
                reportesPdfService.addCellTb(tablaNota, new Paragraph("Distancia a la escombrera:", times8bold), prmsFilaIzquierda)
                reportesPdfService.addCellTb(tablaNota, new Paragraph("${obra?.distanciaDesalojo}" + "km", times8normal), prmsFilaIzquierda)
            }
            reportesPdfService.addCellTb(tablaNota, new Paragraph("Nota:", times8bold), prmsFilaIzquierda)
            reportesPdfService.addCellTb(tablaNota, new Paragraph("Los cálculos se hacen con todos los " +
                    "decimales y el resultado final se lo redondea a dos decimales.", times8normal), prmsFilaIzquierda)

            document.add(headers)
            document.add(tablaCoeficiente)
            document.add(tablaEquipos)
            document.add(tablaManoObra)
            document.add(tablaMateriales)
            if(params.desglose != '0'){
                document.add(tablaTransporte)
            }
            document.add(tablaIndirectos)
            document.add(tablaTotales)
            document.add(tablaNota)
        }

        document.close();
        pdfw.close()
        byte[] b = baos.toByteArray();
        response.setContentType("application/pdf")
        response.setHeader("Content-disposition", "attachment; filename=" + name)
        response.setContentLength(b.length)
        response.getOutputStream().write(b)
    }


    def reporteRubrosTransporteGrupo(){
        println("params rrtgr " + params)

        def rubros = []

        def parts = params.id.split("_")

        switch (parts[0]) {
            case "sg":
                def departamentos = DepartamentoItem.findAllBySubgrupo(SubgrupoItems.get(parts[1].toLong()))
                rubros = Item.findAllByDepartamentoInList(departamentos)
                break;
            case "dp":
                rubros = Item.findAllByDepartamento(DepartamentoItem.get(parts[1].toLong()))
                break;
            case "rb":
                rubros = [Item.get(parts[1].toLong())]
                break;
        }

        def fecha = new Date().parse("dd-MM-yyyy", params.fecha)
        def lugar = params.lugar
        def indi = params.indi
        def listas = params.listas
        try {
            indi = indi.toDouble()
        } catch (e) {
            println "error parse " + e
            indi = 21.5
        }

        def prmsHeaderHoja = [border: Color.WHITE]
        def prmsFila = [border: Color.WHITE, align: Element.ALIGN_CENTER, valign: Element.ALIGN_MIDDLE]
        def prmsFilaIzquierda = [border: Color.WHITE, align : Element.ALIGN_LEFT, valign: Element.ALIGN_LEFT]
        def prmsFilaDerecha = [border: Color.WHITE, align : Element.ALIGN_RIGHT, valign: Element.ALIGN_RIGHT]
        def prmsHeaderHoja2 = [border: Color.WHITE, colspan: 9]
        def prmsHeader = [border: Color.WHITE, colspan: 7, bg: new Color(73, 175, 205),
                          align : Element.ALIGN_CENTER, valign: Element.ALIGN_MIDDLE]
        def prmsHeader2 = [border: Color.WHITE, colspan: 3, bg: new Color(73, 175, 205),
                           align : Element.ALIGN_CENTER, valign: Element.ALIGN_MIDDLE]
        def prmsCellHead = [border: Color.WHITE, bg: new Color(73, 175, 205),
                            align : Element.ALIGN_CENTER, valign: Element.ALIGN_MIDDLE]
        def prmsCellCenter = [border: Color.BLACK, align: Element.ALIGN_CENTER, valign: Element.ALIGN_MIDDLE]
        def prmsCellRight = [border: Color.BLACK, align: Element.ALIGN_RIGHT, valign: Element.ALIGN_RIGHT]
        def prmsCellLeft = [border: Color.BLACK, valign: Element.ALIGN_MIDDLE]
        def prmsSubtotal = [border: Color.BLACK, colspan: 6,
                            align : Element.ALIGN_RIGHT, valign: Element.ALIGN_MIDDLE]
        def prmsNum = [border: Color.BLACK, align: Element.ALIGN_RIGHT, valign: Element.ALIGN_MIDDLE]

        def celdaCabecera = [border: Color.BLACK, bg: new Color(220, 220, 220), align: Element.ALIGN_CENTER, valign: Element.ALIGN_MIDDLE, bordeBot: "1"]
//        def celdaCabecera = [bg: new Color(220, 220, 220), align: Element.ALIGN_CENTER, valign: Element.ALIGN_MIDDLE, bordeBot: "1"]

        def celdaCabeceraIzquierda = [bct: Color.BLACK, bcl: Color.WHITE, bcr:Color.WHITE, bcb: Color.WHITE, align: Element.ALIGN_LEFT, valign: Element.ALIGN_LEFT]
        def celdaCabeceraDerecha = [bct: Color.BLACK, bcl: Color.WHITE, bcr:Color.WHITE, bcb: Color.WHITE, align: Element.ALIGN_RIGHT, valign: Element.ALIGN_RIGHT]
        def celdaCabeceraCentro = [bct: Color.BLACK, bcl: Color.WHITE, bcr:Color.WHITE, bcb: Color.WHITE, align: Element.ALIGN_CENTER, valign: Element.ALIGN_CENTER]
        def celdaCabeceraCentro2 = [bcb: Color.BLACK, bcl: Color.WHITE, bcr:Color.WHITE, bct: Color.WHITE, align: Element.ALIGN_CENTER, valign: Element.ALIGN_CENTER]
        def celdaCabeceraDerecha2 = [bcb: Color.BLACK, bcl: Color.WHITE, bcr:Color.WHITE, bct: Color.WHITE, align: Element.ALIGN_RIGHT, valign: Element.ALIGN_RIGHT]
        def celdaCabeceraIzquierda2 = [bcb: Color.BLACK, bcl: Color.WHITE, bcr:Color.WHITE, bct: Color.WHITE, align: Element.ALIGN_LEFT, valign: Element.ALIGN_LEFT]

        def tituloRubro = [height: 25, border: Color.WHITE, colspan: 12, align : Element.ALIGN_LEFT, valign: Element.ALIGN_MIDDLE]

        def prms = [prmsHeaderHoja: prmsHeaderHoja, prmsHeader: prmsHeader, prmsHeader2: prmsHeader2,
                    prmsCellHead: prmsCellHead, prmsCell: prmsCellCenter, prmsCellLeft: prmsCellLeft, prmsSubtotal: prmsSubtotal, prmsNum: prmsNum, prmsHeaderHoja2: prmsHeaderHoja2, prmsCellRight: prmsCellRight]

        Font times12bold = new Font(Font.TIMES_ROMAN, 12, Font.BOLD);
        Font times14bold = new Font(Font.TIMES_ROMAN, 14, Font.BOLD);
        Font times10bold = new Font(Font.TIMES_ROMAN, 10, Font.BOLD);
        Font times10normal = new Font(Font.TIMES_ROMAN, 10, Font.NORMAL);
        Font times8bold = new Font(Font.TIMES_ROMAN, 8, Font.BOLD)
        Font times8normal = new Font(Font.TIMES_ROMAN, 8, Font.NORMAL)
        Font times7bold = new Font(Font.TIMES_ROMAN, 7, Font.BOLD)
        Font times7normal = new Font(Font.TIMES_ROMAN, 7, Font.NORMAL)
        Font times10boldWhite = new Font(Font.TIMES_ROMAN, 10, Font.BOLD);
        Font times8boldWhite = new Font(Font.TIMES_ROMAN, 8, Font.BOLD)
        times8boldWhite.setColor(Color.WHITE)
        times10boldWhite.setColor(Color.WHITE)

        def baos = new ByteArrayOutputStream()
        def name = "reporteRubrosTransporte_" + new Date().format("ddMMyyyy_hhmm") + ".pdf";

        Document document
        document = new Document(PageSize.A4)
        document.setMargins(60, 24, 45, 45);

        def pdfw = PdfWriter.getInstance(document, baos);
        document.open();
        document.addTitle("Rubros " + new Date().format("dd_MM_yyyy"));
        document.addSubject("Generado por el sistema Obras");
        document.addKeywords("documentosObra, janus, rubros");
        document.addAuthor("OBRAS");
        document.addCreator("Tedein SA");

        rubros.eachWithIndex{ rubro, indice->

            document.newPage();

            def nombre = rubro?.nombre

            def parametros = "" + rubro.id + ",'" + fecha.format("yyyy-MM-dd") + "'," + listas + "," + params.dsp0 + "," + params.dsp1 + "," + params.dsv0 + "," + params.dsv1 + "," + params.dsv2 + "," + params.chof + "," + params.volq
            preciosService.ac_rbroV2(rubro.id, fecha.format("yyyy-MM-dd"), params.lugar)
            def res = preciosService.rb_precios(parametros, "")

            def total = 0, totalHer = 0, totalMan = 0, totalMat = 0
            def band = 0
            def bandMat = 0
            def obra
            def bandTrans = params.trans

            if (params.obra) {
                obra = Obra.get(params.obra)
            }

            Paragraph headers = new Paragraph();
            addEmptyLine(headers, 1);
            headers.setAlignment(Element.ALIGN_CENTER);
            headers.add(new Paragraph("G.A.D. LOS RÍOS", times14bold));
            headers.add(new Paragraph("COORDINACIÓN DE FIJACIÓN DE PRECIOS UNITARIOS - ANÁLISIS DE PRECIOS UNITARIOS", times10bold));
            headers.add(new Paragraph("", times14bold));

            PdfPTable tablaCoeficiente = new PdfPTable(6);
            tablaCoeficiente.setWidthPercentage(100);
            tablaCoeficiente.setWidths(arregloEnteros([13,20, 26,10, 15,15]))

            reportesPdfService.addCellTb(tablaCoeficiente, new Paragraph("Fecha: ", times10bold), [border: Color.WHITE, align: Element.ALIGN_LEFT])
            reportesPdfService.addCellTb(tablaCoeficiente, new Paragraph((new Date().format("dd-MM-yyyy") ?: ''), times10normal), [border: Color.WHITE, colspan: 3])
            reportesPdfService.addCellTb(tablaCoeficiente, new Paragraph("Fecha Act. P.U: ", times10bold), prmsHeaderHoja)
            reportesPdfService.addCellTb(tablaCoeficiente, new Paragraph((fecha?.format("dd-MM-yyyy") ?: '') , times10normal), prmsHeaderHoja)

            reportesPdfService.addCellTb(tablaCoeficiente, new Paragraph("Código: ", times10bold), prmsHeaderHoja)
            reportesPdfService.addCellTb(tablaCoeficiente, new Paragraph((rubro?.codigo ?: ''), times10normal), prmsHeaderHoja)
            reportesPdfService.addCellTb(tablaCoeficiente, new Paragraph("Código de Especificación: ", times10bold), prmsHeaderHoja)
            reportesPdfService.addCellTb(tablaCoeficiente, new Paragraph((rubro?.codigoEspecificacion ?: ''), times10normal), prmsHeaderHoja)
            reportesPdfService.addCellTb(tablaCoeficiente, new Paragraph("Unidad: ", times10bold), prmsHeaderHoja)
            reportesPdfService.addCellTb(tablaCoeficiente, new Paragraph((rubro?.unidad?.codigo ?: ''), times10normal), prmsHeaderHoja)

            reportesPdfService.addCellTb(tablaCoeficiente, new Paragraph("Descripción: ", times10bold), prmsHeaderHoja)
            reportesPdfService.addCellTb(tablaCoeficiente, new Paragraph((rubro?.nombre ?: ''), times10normal), [border: Color.WHITE, colspan: 5])

            //EQUIPOS
            PdfPTable tablaEquipos = new PdfPTable(7);
            tablaEquipos.setWidthPercentage(100);
            tablaEquipos.setWidths(arregloEnteros([8,40,8,9,8,10,8]))

            reportesPdfService.addCellTb(tablaEquipos, new Paragraph("EQUIPOS", times12bold), tituloRubro)

            reportesPdfService.addCellTb(tablaEquipos, new Paragraph("CÓDIGO", times7bold), celdaCabecera)
            reportesPdfService.addCellTb(tablaEquipos, new Paragraph("DESCRIPCIÓN", times7bold), celdaCabecera)
            reportesPdfService.addCellTb(tablaEquipos, new Paragraph("CANTIDAD", times7bold), celdaCabecera)
            reportesPdfService.addCellTb(tablaEquipos, new Paragraph("TARIFA(\$/H)", times7bold), celdaCabecera)
            reportesPdfService.addCellTb(tablaEquipos, new Paragraph("COSTOS(\$)", times7bold), celdaCabecera)
            reportesPdfService.addCellTb(tablaEquipos, new Paragraph("RENDIMIENTO", times7bold), celdaCabecera)
            reportesPdfService.addCellTb(tablaEquipos, new Paragraph("C.TOTAL(\$)", times7bold), celdaCabecera)

            res.eachWithIndex { r, i ->
                if (r["grpocdgo"] == 3) {
                    reportesPdfService.addCellTb(tablaEquipos, new Paragraph(r["itemcdgo"], times8normal), prmsFilaIzquierda)
                    reportesPdfService.addCellTb(tablaEquipos, new Paragraph(r["itemnmbr"], times8normal), prmsFilaIzquierda)
                    reportesPdfService.addCellTb(tablaEquipos, new Paragraph(numero(r["rbrocntd"], 5)?.toString(), times8normal), prmsFilaDerecha)
                    reportesPdfService.addCellTb(tablaEquipos, new Paragraph(numero(r["rbpcpcun"], 5)?.toString(), times8normal), prmsFilaDerecha)
                    reportesPdfService.addCellTb(tablaEquipos, new Paragraph((numero((r["rbpcpcun"] * r["rbrocntd"]), 5))?.toString(), times8normal), prmsFilaDerecha)
                    reportesPdfService.addCellTb(tablaEquipos, new Paragraph(numero(r["rndm"], 5)?.toString(), times8normal), prmsFilaDerecha)
                    reportesPdfService.addCellTb(tablaEquipos, new Paragraph(numero(r["parcial"], 5)?.toString(), times8normal), prmsFilaDerecha)
                    totalHer += r["parcial"]
                }
            }

            reportesPdfService.addCellTb(tablaEquipos, new Paragraph("", times14bold), [border: Color.WHITE, colspan: 5])
            reportesPdfService.addCellTb(tablaEquipos, new Paragraph("TOTAL", times8bold), prmsFilaDerecha)
            reportesPdfService.addCellTb(tablaEquipos, new Paragraph(numero(totalHer, 5)?.toString(), times8bold), prmsFilaDerecha)
            reportesPdfService.addCellTb(tablaEquipos, new Paragraph("", times14bold), [border: Color.WHITE, colspan: 7])

            //MANO DE OBRA
            PdfPTable tablaManoObra = new PdfPTable(7);
            tablaManoObra.setWidthPercentage(100);
            tablaManoObra.setWidths(arregloEnteros([6,42,8,9,8,10,8]))

            reportesPdfService.addCellTb(tablaManoObra, new Paragraph("MANO DE OBRA", times12bold), tituloRubro)

            reportesPdfService.addCellTb(tablaManoObra, new Paragraph("CÓDIGO", times7bold), celdaCabecera)
            reportesPdfService.addCellTb(tablaManoObra, new Paragraph("DESCRIPCIÓN", times7bold), celdaCabecera)
            reportesPdfService.addCellTb(tablaManoObra, new Paragraph("CANTIDAD", times7bold), celdaCabecera)
            reportesPdfService.addCellTb(tablaManoObra, new Paragraph("JORNAL(\$/H)", times7bold), celdaCabecera)
            reportesPdfService.addCellTb(tablaManoObra, new Paragraph("COSTOS(\$)", times7bold), celdaCabecera)
            reportesPdfService.addCellTb(tablaManoObra, new Paragraph("RENDIMIENTO", times7bold), celdaCabecera)
            reportesPdfService.addCellTb(tablaManoObra, new Paragraph("C.TOTAL(\$)", times7bold), celdaCabecera)

            res.eachWithIndex { r, i ->
                if (r["grpocdgo"] == 2) {
                    reportesPdfService.addCellTb(tablaManoObra, new Paragraph(r["itemcdgo"], times8normal), prmsFilaIzquierda)
                    reportesPdfService.addCellTb(tablaManoObra, new Paragraph(r["itemnmbr"], times8normal), prmsFilaIzquierda)
                    reportesPdfService.addCellTb(tablaManoObra, new Paragraph(numero(r["rbrocntd"], 5)?.toString(), times8normal), prmsFilaDerecha)
                    reportesPdfService.addCellTb(tablaManoObra, new Paragraph(numero(r["rbpcpcun"], 5)?.toString(), times8normal), prmsFilaDerecha)
                    reportesPdfService.addCellTb(tablaManoObra, new Paragraph((numero((r["rbpcpcun"] * r["rbrocntd"]), 5))?.toString(), times8normal), prmsFilaDerecha)
                    reportesPdfService.addCellTb(tablaManoObra, new Paragraph(numero(r["rndm"], 5)?.toString(), times8normal), prmsFilaDerecha)
                    reportesPdfService.addCellTb(tablaManoObra, new Paragraph(numero(r["parcial"], 5)?.toString(), times8normal), prmsFilaDerecha)
                    totalMan += r["parcial"]
                }
            }

            reportesPdfService.addCellTb(tablaManoObra, new Paragraph("", times8bold), [border: Color.WHITE, colspan: 5])
            reportesPdfService.addCellTb(tablaManoObra, new Paragraph("TOTAL", times8bold), prmsFilaDerecha)
            reportesPdfService.addCellTb(tablaManoObra, new Paragraph(numero(totalMan, 5)?.toString(), times8bold), prmsFilaDerecha)
            reportesPdfService.addCellTb(tablaManoObra, new Paragraph("", times8bold), [border: Color.WHITE, colspan: 7])

            //MATERIALES
            PdfPTable tablaMateriales = new PdfPTable(6);
            tablaMateriales.setWidthPercentage(100);
            tablaMateriales.setWidths(arregloEnteros([8,48,9,8,10,8]))

            if(params.trans == 'no'){
                reportesPdfService.addCellTb(tablaMateriales, new Paragraph("MATERIALES INCLUIDO TRANSPORTE", times12bold), tituloRubro)
            }else{
                reportesPdfService.addCellTb(tablaMateriales, new Paragraph("MATERIALES", times12bold), tituloRubro)
            }

            reportesPdfService.addCellTb(tablaMateriales, new Paragraph("CÓDIGO", times7bold), celdaCabecera)
            reportesPdfService.addCellTb(tablaMateriales, new Paragraph("DESCRIPCIÓN", times7bold), celdaCabecera)
            reportesPdfService.addCellTb(tablaMateriales, new Paragraph("UNIDAD", times7bold), celdaCabecera)
            reportesPdfService.addCellTb(tablaMateriales, new Paragraph("CANTIDAD", times7bold), celdaCabecera)
            reportesPdfService.addCellTb(tablaMateriales, new Paragraph("UNITARIO(\$)", times7bold), celdaCabecera)
            reportesPdfService.addCellTb(tablaMateriales, new Paragraph("C.TOTAL(\$)", times7bold), celdaCabecera)

            res.eachWithIndex { r, i ->
                if (r["grpocdgo"] == 1) {
                    bandMat = 1
                    reportesPdfService.addCellTb(tablaMateriales, new Paragraph(r["itemcdgo"], times8normal), prmsFilaIzquierda)
                    reportesPdfService.addCellTb(tablaMateriales, new Paragraph(r["itemnmbr"], times8normal), prmsFilaIzquierda)
                    reportesPdfService.addCellTb(tablaMateriales, new Paragraph(r["unddcdgo"], times8normal), prmsFila)
                    reportesPdfService.addCellTb(tablaMateriales, new Paragraph(numero(r["rbrocntd"], 5)?.toString(), times8normal), prmsFila)
                    if (params.trans != 'no') {
                        reportesPdfService.addCellTb(tablaMateriales, new Paragraph(numero(r["rbpcpcun"], 5)?.toString(), times8normal), prmsFilaDerecha)
                        reportesPdfService.addCellTb(tablaMateriales, new Paragraph(numero(r["parcial"], 5)?.toString(), times8normal), prmsFilaDerecha)
                        totalMat += r["parcial"]
                    }else{
                        reportesPdfService.addCellTb(tablaMateriales, new Paragraph(numero((r["rbpcpcun"] + r["parcial_t"] / r["rbrocntd"]), 5)?.toString(), times8normal), prmsFilaDerecha)
                        reportesPdfService.addCellTb(tablaMateriales, new Paragraph(numero((r["parcial"] + r["parcial_t"]), 5)?.toString(), times8normal), prmsFilaDerecha)
                        totalMat += (r["parcial"] + r["parcial_t"])
                    }
                }
            }

            reportesPdfService.addCellTb(tablaMateriales, new Paragraph("", times8bold), [border: Color.WHITE, colspan: 4])
            reportesPdfService.addCellTb(tablaMateriales, new Paragraph("TOTAL", times8bold), prmsFilaDerecha)
            reportesPdfService.addCellTb(tablaMateriales, new Paragraph(numero(totalMat, 5)?.toString(), times8bold), prmsFilaDerecha)
            reportesPdfService.addCellTb(tablaMateriales, new Paragraph("", times8bold), [border: Color.WHITE, colspan: 6])

            //TRANSPORTE
            PdfPTable tablaTransporte = new PdfPTable(8);
            tablaTransporte.setWidthPercentage(100);
            tablaTransporte.setWidths(arregloEnteros([11,25,8,11,11,12,10,10]))

            reportesPdfService.addCellTb(tablaTransporte, new Paragraph("TRANSPORTE", times12bold), tituloRubro)

            reportesPdfService.addCellTb(tablaTransporte, new Paragraph("CÓDIGO", times7bold), celdaCabecera)
            reportesPdfService.addCellTb(tablaTransporte, new Paragraph("DESCRIPCIÓN", times7bold), celdaCabecera)
            reportesPdfService.addCellTb(tablaTransporte, new Paragraph("UNIDAD", times7bold), celdaCabecera)
            reportesPdfService.addCellTb(tablaTransporte, new Paragraph("PES/VOL", times7bold), celdaCabecera)
            reportesPdfService.addCellTb(tablaTransporte, new Paragraph("CANTIDAD", times7bold), celdaCabecera)
            reportesPdfService.addCellTb(tablaTransporte, new Paragraph("DISTANCIA", times7bold), celdaCabecera)
            reportesPdfService.addCellTb(tablaTransporte, new Paragraph("TARIFA", times7bold), celdaCabecera)
            reportesPdfService.addCellTb(tablaTransporte, new Paragraph("C.TOTAL(\$)", times7bold), celdaCabecera)

            res.eachWithIndex { r, i ->
                if (r["grpocdgo"]== 1 && params.desgltrans != 'no') {
                    reportesPdfService.addCellTb(tablaTransporte, new Paragraph(r["itemcdgo"], times8normal), prmsFilaIzquierda)
                    reportesPdfService.addCellTb(tablaTransporte, new Paragraph(r["itemnmbr"], times8normal), prmsFilaIzquierda)
                    if(r["tplscdgo"].trim() =='P' || r["tplscdgo"].trim() =='P1' ){
                        reportesPdfService.addCellTb(tablaTransporte, new Paragraph("ton-km", times8normal), prmsFila)
                    }else{
                        if(r["tplscdgo"].trim() =='V' || r["tplscdgo"].trim() =='V1' || r["tplscdgo"].trim() =='V2') {
                            reportesPdfService.addCellTb(tablaTransporte, new Paragraph("m3-km", times8normal), prmsFila)
                        }else{
                            reportesPdfService.addCellTb(tablaTransporte, new Paragraph(r["unddcdgo"], times8normal), prmsFila)
                        }
                    }
                    reportesPdfService.addCellTb(tablaTransporte, new Paragraph(numero(r["itempeso"], 5)?.toString(), times8normal), prmsFila)
                    reportesPdfService.addCellTb(tablaTransporte, new Paragraph(numero(r["rbrocntd"], 5)?.toString(), times8normal), prmsFila)
                    reportesPdfService.addCellTb(tablaTransporte, new Paragraph(numero(r["distancia"], 5)?.toString(), times8normal), prmsFila)
                    reportesPdfService.addCellTb(tablaTransporte, new Paragraph(numero(r["tarifa"], 5)?.toString(), times8normal), prmsFila)
                    reportesPdfService.addCellTb(tablaTransporte, new Paragraph(numero(r["parcial_t"], 5)?.toString(), times8normal), prmsFilaDerecha)
                    total += r["parcial_t"]
                }
            }

            reportesPdfService.addCellTb(tablaTransporte, new Paragraph("", times8bold), [border: Color.WHITE, colspan: 6])
            reportesPdfService.addCellTb(tablaTransporte, new Paragraph("TOTAL", times8bold), prmsFila)
            reportesPdfService.addCellTb(tablaTransporte, new Paragraph(numero(total, 5)?.toString(), times8bold), prmsFilaDerecha)
            reportesPdfService.addCellTb(tablaTransporte, new Paragraph("", times8bold), [border: Color.WHITE, colspan: 6])

            //COSTOS INDIRECTOS
            def totalRubro

            if (!params.trans) {
                totalRubro = total + totalHer + totalMan + totalMat
            } else {
                totalRubro = totalHer + totalMan + totalMat
            }

            def totalIndi = totalRubro?.toDouble() * indi / 100

            PdfPTable tablaIndirectos = new PdfPTable(3);
            tablaIndirectos.setWidthPercentage(70);
            tablaIndirectos.setWidths(arregloEnteros([50,25,25]))
            tablaIndirectos.horizontalAlignment = Element.ALIGN_LEFT;

            reportesPdfService.addCellTb(tablaIndirectos, new Paragraph("COSTOS INDIRECTOS", times12bold), tituloRubro)

            reportesPdfService.addCellTb(tablaIndirectos, new Paragraph("DESCRIPCIÓN", times7bold), celdaCabecera)
            reportesPdfService.addCellTb(tablaIndirectos, new Paragraph("PORCENTAJE", times7bold), celdaCabecera)
            reportesPdfService.addCellTb(tablaIndirectos, new Paragraph("VALOR", times7bold), celdaCabecera)

            reportesPdfService.addCellTb(tablaIndirectos, new Paragraph("COSTOS INDIRECTOS", times8normal), prmsFilaIzquierda)
            reportesPdfService.addCellTb(tablaIndirectos, new Paragraph(numero(indi, 1)?.toString() + "%", times8normal), prmsFila)
            reportesPdfService.addCellTb(tablaIndirectos, new Paragraph(numero(totalIndi, 5)?.toString(), times8normal), prmsFila)
            reportesPdfService.addCellTb(tablaIndirectos, new Paragraph("", times8bold), [border: Color.WHITE, colspan: 3])

            PdfPTable tablaTotales = new PdfPTable(2);
            tablaTotales.setWidthPercentage(40);
            tablaTotales.setWidths(arregloEnteros([50,25]))
            tablaTotales.horizontalAlignment = Element.ALIGN_RIGHT;

            reportesPdfService.addCellTb(tablaTotales, new Paragraph("COSTO UNITARIO DIRECTO", times8bold), celdaCabeceraIzquierda)
            reportesPdfService.addCellTb(tablaTotales, new Paragraph(numero(totalRubro, 2)?.toString(), times8bold), celdaCabeceraDerecha)

            reportesPdfService.addCellTb(tablaTotales, new Paragraph("COSTOS INDIRECTO", times8bold), prmsFilaIzquierda)
            reportesPdfService.addCellTb(tablaTotales, new Paragraph(numero(totalIndi, 2)?.toString(), times8bold), prmsFilaDerecha)

            reportesPdfService.addCellTb(tablaTotales, new Paragraph("COSTO TOTAL DEL RUBRO", times8bold), prmsFilaIzquierda)
            reportesPdfService.addCellTb(tablaTotales, new Paragraph(numero((totalRubro + totalIndi), 2)?.toString(), times8bold), prmsFilaDerecha)

            reportesPdfService.addCellTb(tablaTotales, new Paragraph("PRECIO UNITARIO \$USD", times8bold), celdaCabeceraIzquierda2)
            reportesPdfService.addCellTb(tablaTotales, new Paragraph(numero((totalRubro + totalIndi), 2)?.toString(), times8bold), celdaCabeceraDerecha2)

            PdfPTable tablaNota = new PdfPTable(2);
            tablaNota.setWidthPercentage(100);
            tablaNota.setWidths(arregloEnteros([6, 94]))
//            if(rubro?.codigo?.split('-')[0] == 'TR'){
//                reportesPdfService.addCellTb(tablaNota, new Paragraph("Distancia a la escombrera:", times8bold), prmsFilaIzquierda)
//                reportesPdfService.addCellTb(tablaNota, new Paragraph("${obra?.distanciaDesalojo}" + "km", times8normal), prmsFilaIzquierda)
//            }
            reportesPdfService.addCellTb(tablaNota, new Paragraph("Nota:", times8bold), prmsFilaIzquierda)
            reportesPdfService.addCellTb(tablaNota, new Paragraph("Los cálculos se hacen con todos los " +
                    "decimales y el resultado final se lo redondea a dos decimales.", times8normal), prmsFilaIzquierda)

            document.add(headers)
            document.add(tablaCoeficiente)
            document.add(tablaEquipos)
            document.add(tablaManoObra)
            document.add(tablaMateriales)
            if(params.trans != 'no'){
                document.add(tablaTransporte)
            }
            document.add(tablaIndirectos)
            document.add(tablaTotales)
            document.add(tablaNota)
        }

        document.close();
        pdfw.close()
        byte[] b = baos.toByteArray();
        response.setContentType("application/pdf")
        response.setHeader("Content-disposition", "attachment; filename=" + name)
        response.setContentLength(b.length)
        response.getOutputStream().write(b)
    }


    def reporteRubrosVolumen(){
        println("params rrvo " + params)

        def obra = Obra.get(params.obra)
        def fecha1
        def fecha2

        if(params.fecha){
            fecha1 = new Date().parse("dd-MM-yyyy", params.fecha)
        }else {
        }

        if(params.fechaSalida){
            fecha2 = new Date().parse("dd-MM-yyyy", params.fechaSalida)
        }

        def vol1 = VolumenesObra.get(params.id)
        def rubro = Item.get(vol1.item.id)
        def indi = obra.totales

        try {
            indi = indi.toDouble()
        } catch (e) {
            println "error parse " + e
            indi = 21.5
        }

        preciosService.ac_rbroObra(obra.id)
        def res = preciosService.precioUnitarioVolumenObraAsc("*", obra.id, rubro.id)

        def total = 0, totalHer = 0, totalMan = 0, totalMat = 0, totalHerRel = 0, totalHerVae = 0, totalManRel = 0, totalManVae = 0, totalMatRel = 0, totalMatVae = 0, totalTRel=0, totalTVae=0
        def band = 0
        def bandMat = 0
        def bandTrans = params.desglose

        def prmsHeaderHoja = [border: Color.WHITE]
        def prmsFila = [border: Color.WHITE, align: Element.ALIGN_CENTER, valign: Element.ALIGN_MIDDLE]
        def prmsFilaIzquierda = [border: Color.WHITE, align : Element.ALIGN_LEFT, valign: Element.ALIGN_LEFT]
        def prmsFilaDerecha = [border: Color.WHITE, align : Element.ALIGN_RIGHT, valign: Element.ALIGN_RIGHT]
        def prmsHeaderHoja2 = [border: Color.WHITE, colspan: 9]
        def prmsHeader = [border: Color.WHITE, colspan: 7, bg: new Color(73, 175, 205),
                          align : Element.ALIGN_CENTER, valign: Element.ALIGN_MIDDLE]
        def prmsHeader2 = [border: Color.WHITE, colspan: 3, bg: new Color(73, 175, 205),
                           align : Element.ALIGN_CENTER, valign: Element.ALIGN_MIDDLE]
        def prmsCellHead = [border: Color.WHITE, bg: new Color(73, 175, 205),
                            align : Element.ALIGN_CENTER, valign: Element.ALIGN_MIDDLE]
        def prmsCellCenter = [border: Color.BLACK, align: Element.ALIGN_CENTER, valign: Element.ALIGN_MIDDLE]
        def prmsCellRight = [border: Color.BLACK, align: Element.ALIGN_RIGHT, valign: Element.ALIGN_RIGHT]
        def prmsCellLeft = [border: Color.BLACK, valign: Element.ALIGN_MIDDLE]
        def prmsSubtotal = [border: Color.BLACK, colspan: 6,
                            align : Element.ALIGN_RIGHT, valign: Element.ALIGN_MIDDLE]
        def prmsNum = [border: Color.BLACK, align: Element.ALIGN_RIGHT, valign: Element.ALIGN_MIDDLE]

        def celdaCabecera = [border: Color.BLACK, bg: new Color(220, 220, 220), align: Element.ALIGN_CENTER, valign: Element.ALIGN_MIDDLE, bordeBot: "1"]
        def celdaCabeceraIzquierda = [bct: Color.BLACK, bcl: Color.WHITE, bcr:Color.WHITE, bcb: Color.WHITE, align: Element.ALIGN_LEFT, valign: Element.ALIGN_LEFT]
        def celdaCabeceraDerecha = [bct: Color.BLACK, bcl: Color.WHITE, bcr:Color.WHITE, bcb: Color.WHITE, align: Element.ALIGN_RIGHT, valign: Element.ALIGN_RIGHT]
        def celdaCabeceraCentro = [bct: Color.BLACK, bcl: Color.WHITE, bcr:Color.WHITE, bcb: Color.WHITE, align: Element.ALIGN_CENTER, valign: Element.ALIGN_CENTER]
        def celdaCabeceraCentro2 = [bcb: Color.BLACK, bcl: Color.WHITE, bcr:Color.WHITE, bct: Color.WHITE, align: Element.ALIGN_CENTER, valign: Element.ALIGN_CENTER]
        def celdaCabeceraDerecha2 = [bcb: Color.BLACK, bcl: Color.WHITE, bcr:Color.WHITE, bct: Color.WHITE, align: Element.ALIGN_RIGHT, valign: Element.ALIGN_RIGHT]
        def celdaCabeceraIzquierda2 = [bcb: Color.BLACK, bcl: Color.WHITE, bcr:Color.WHITE, bct: Color.WHITE, align: Element.ALIGN_LEFT, valign: Element.ALIGN_LEFT]

        def tituloRubro = [height: 25, border: Color.WHITE, colspan: 12, align : Element.ALIGN_LEFT, valign: Element.ALIGN_MIDDLE]

        def prms = [prmsHeaderHoja: prmsHeaderHoja, prmsHeader: prmsHeader, prmsHeader2: prmsHeader2,
                    prmsCellHead: prmsCellHead, prmsCell: prmsCellCenter, prmsCellLeft: prmsCellLeft, prmsSubtotal: prmsSubtotal, prmsNum: prmsNum, prmsHeaderHoja2: prmsHeaderHoja2, prmsCellRight: prmsCellRight]

        Font times12bold = new Font(Font.TIMES_ROMAN, 12, Font.BOLD);
        Font times14bold = new Font(Font.TIMES_ROMAN, 14, Font.BOLD);
        Font times10bold = new Font(Font.TIMES_ROMAN, 10, Font.BOLD);
        Font times10normal = new Font(Font.TIMES_ROMAN, 10, Font.NORMAL);
        Font times8bold = new Font(Font.TIMES_ROMAN, 8, Font.BOLD)
        Font times8normal = new Font(Font.TIMES_ROMAN, 8, Font.NORMAL)
        Font times7bold = new Font(Font.TIMES_ROMAN, 7, Font.BOLD)
        Font times7normal = new Font(Font.TIMES_ROMAN, 7, Font.NORMAL)
        Font times10boldWhite = new Font(Font.TIMES_ROMAN, 10, Font.BOLD);
        Font times8boldWhite = new Font(Font.TIMES_ROMAN, 8, Font.BOLD)
        times8boldWhite.setColor(Color.WHITE)
        times10boldWhite.setColor(Color.WHITE)

        def baos = new ByteArrayOutputStream()
        def name = ''

        if(params.desglose == '0'){
            name = "reporteRubrosSinDesglose_" + new Date().format("ddMMyyyy_hhmm") + ".pdf";
        }else{
            name = "reporteRubrosConDesglose_" + new Date().format("ddMMyyyy_hhmm") + ".pdf";
        }

        Document document
        document = new Document(PageSize.A4)
        document.setMargins(60, 24, 45, 45);

        def pdfw = PdfWriter.getInstance(document, baos);
        document.open();
        document.addTitle("Rubros " + new Date().format("dd_MM_yyyy"));
        document.addSubject("Generado por el sistema Obras");
        document.addKeywords("documentosObra, janus, rubros");
        document.addAuthor("OBRAS");
        document.addCreator("Tedein SA");

        def nombre = rubro?.nombre

        Paragraph headers = new Paragraph();
        addEmptyLine(headers, 1);
        headers.setAlignment(Element.ALIGN_CENTER);
        headers.add(new Paragraph("G.A.D. LOS RÍOS", times14bold));
        headers.add(new Paragraph("COORDINACIÓN DE FIJACIÓN DE PRECIOS UNITARIOS - ANÁLISIS DE PRECIOS UNITARIOS", times10bold));
        headers.add(new Paragraph("", times14bold));

        PdfPTable tablaCoeficiente = new PdfPTable(6);
        tablaCoeficiente.setWidthPercentage(100);
        tablaCoeficiente.setWidths(arregloEnteros([18,15, 26,10, 15,15]))

        reportesPdfService.addCellTb(tablaCoeficiente, new Paragraph("Fecha: ", times10bold), [border: Color.WHITE, align: Element.ALIGN_LEFT])
        reportesPdfService.addCellTb(tablaCoeficiente, new Paragraph((fecha2?.format("dd-MM-yyyy") ?: ''), times10normal), [border: Color.WHITE, colspan: 3])
        reportesPdfService.addCellTb(tablaCoeficiente, new Paragraph("Fecha Act. P.U: ", times10bold), prmsHeaderHoja)
        reportesPdfService.addCellTb(tablaCoeficiente, new Paragraph((fecha1?.format("dd-MM-yyyy") ?: '') , times10normal), prmsHeaderHoja)

        reportesPdfService.addCellTb(tablaCoeficiente, new Paragraph("Código Obra: ", times10bold), prmsHeaderHoja)
        reportesPdfService.addCellTb(tablaCoeficiente, new Paragraph((obra?.codigo ?: ''), times10normal), [border: Color.WHITE, colspan: 5])

        reportesPdfService.addCellTb(tablaCoeficiente, new Paragraph("Código de rubro: ", times10bold), prmsHeaderHoja)
        reportesPdfService.addCellTb(tablaCoeficiente, new Paragraph((rubro?.codigo ?: ''), times10normal), prmsHeaderHoja)
        reportesPdfService.addCellTb(tablaCoeficiente, new Paragraph("Código de Especificación: ", times10bold), prmsHeaderHoja)
        reportesPdfService.addCellTb(tablaCoeficiente, new Paragraph((rubro?.codigoEspecificacion ?: ''), times10normal), [border: Color.WHITE, colspan: 3])

        reportesPdfService.addCellTb(tablaCoeficiente, new Paragraph("Presupuesto: ", times10bold), prmsHeaderHoja)
        reportesPdfService.addCellTb(tablaCoeficiente, new Paragraph((obra?.nombre ?: ''), times10normal), [border: Color.WHITE, colspan: 3])
        reportesPdfService.addCellTb(tablaCoeficiente, new Paragraph("Unidad: ", times10bold), prmsHeaderHoja)
        reportesPdfService.addCellTb(tablaCoeficiente, new Paragraph((rubro?.unidad?.codigo ?: ''), times10normal), prmsHeaderHoja)

        reportesPdfService.addCellTb(tablaCoeficiente, new Paragraph("Descripción: ", times10bold), prmsHeaderHoja)
        reportesPdfService.addCellTb(tablaCoeficiente, new Paragraph((rubro?.nombre ?: ''), times10normal), [border: Color.WHITE, colspan: 5])

        //EQUIPOS
        PdfPTable tablaEquipos = new PdfPTable(7);
        tablaEquipos.setWidthPercentage(100);
        tablaEquipos.setWidths(arregloEnteros([8,40,8,9,8,10,8]))

        reportesPdfService.addCellTb(tablaEquipos, new Paragraph("EQUIPOS", times12bold), tituloRubro)

        reportesPdfService.addCellTb(tablaEquipos, new Paragraph("CÓDIGO", times7bold), celdaCabecera)
        reportesPdfService.addCellTb(tablaEquipos, new Paragraph("DESCRIPCIÓN", times7bold), celdaCabecera)
        reportesPdfService.addCellTb(tablaEquipos, new Paragraph("CANTIDAD", times7bold), celdaCabecera)
        reportesPdfService.addCellTb(tablaEquipos, new Paragraph("TARIFA(\$/H)", times7bold), celdaCabecera)
        reportesPdfService.addCellTb(tablaEquipos, new Paragraph("COSTOS(\$)", times7bold), celdaCabecera)
        reportesPdfService.addCellTb(tablaEquipos, new Paragraph("RENDIMIENTO", times7bold), celdaCabecera)
        reportesPdfService.addCellTb(tablaEquipos, new Paragraph("C.TOTAL(\$)", times7bold), celdaCabecera)

        res.eachWithIndex { r, i ->
            if (r["grpocdgo"] == 3) {
                reportesPdfService.addCellTb(tablaEquipos, new Paragraph(r["itemcdgo"], times8normal), prmsFilaIzquierda)
                reportesPdfService.addCellTb(tablaEquipos, new Paragraph(r["itemnmbr"], times8normal), prmsFilaIzquierda)
                reportesPdfService.addCellTb(tablaEquipos, new Paragraph(numero(r["rbrocntd"], 5)?.toString(), times8normal), prmsFilaDerecha)
                reportesPdfService.addCellTb(tablaEquipos, new Paragraph(numero(r["rbpcpcun"], 5)?.toString(), times8normal), prmsFilaDerecha)
                reportesPdfService.addCellTb(tablaEquipos, new Paragraph((numero((r["rbpcpcun"] * r["rbrocntd"]), 5))?.toString(), times8normal), prmsFilaDerecha)
                reportesPdfService.addCellTb(tablaEquipos, new Paragraph(numero(r["rndm"], 5)?.toString(), times8normal), prmsFilaDerecha)
                reportesPdfService.addCellTb(tablaEquipos, new Paragraph(numero(r["parcial"], 5)?.toString(), times8normal), prmsFilaDerecha)
                totalHer += r["parcial"]
            }
        }

        reportesPdfService.addCellTb(tablaEquipos, new Paragraph("", times14bold), [border: Color.WHITE, colspan: 5])
        reportesPdfService.addCellTb(tablaEquipos, new Paragraph("TOTAL", times8bold), prmsFilaDerecha)
        reportesPdfService.addCellTb(tablaEquipos, new Paragraph(numero(totalHer, 5)?.toString(), times8bold), prmsFilaDerecha)
        reportesPdfService.addCellTb(tablaEquipos, new Paragraph("", times14bold), [border: Color.WHITE, colspan: 7])

        //MANO DE OBRA
        PdfPTable tablaManoObra = new PdfPTable(7);
        tablaManoObra.setWidthPercentage(100);
        tablaManoObra.setWidths(arregloEnteros([6,42,8,9,8,10,8]))

        reportesPdfService.addCellTb(tablaManoObra, new Paragraph("MANO DE OBRA", times12bold), tituloRubro)

        reportesPdfService.addCellTb(tablaManoObra, new Paragraph("CÓDIGO", times7bold), celdaCabecera)
        reportesPdfService.addCellTb(tablaManoObra, new Paragraph("DESCRIPCIÓN", times7bold), celdaCabecera)
        reportesPdfService.addCellTb(tablaManoObra, new Paragraph("CANTIDAD", times7bold), celdaCabecera)
        reportesPdfService.addCellTb(tablaManoObra, new Paragraph("JORNAL(\$/H)", times7bold), celdaCabecera)
        reportesPdfService.addCellTb(tablaManoObra, new Paragraph("COSTOS(\$)", times7bold), celdaCabecera)
        reportesPdfService.addCellTb(tablaManoObra, new Paragraph("RENDIMIENTO", times7bold), celdaCabecera)
        reportesPdfService.addCellTb(tablaManoObra, new Paragraph("C.TOTAL(\$)", times7bold), celdaCabecera)

        res.eachWithIndex { r, i ->
            if (r["grpocdgo"] == 2) {
                reportesPdfService.addCellTb(tablaManoObra, new Paragraph(r["itemcdgo"], times8normal), prmsFilaIzquierda)
                reportesPdfService.addCellTb(tablaManoObra, new Paragraph(r["itemnmbr"], times8normal), prmsFilaIzquierda)
                reportesPdfService.addCellTb(tablaManoObra, new Paragraph(numero(r["rbrocntd"], 5)?.toString(), times8normal), prmsFilaDerecha)
                reportesPdfService.addCellTb(tablaManoObra, new Paragraph(numero(r["rbpcpcun"], 5)?.toString(), times8normal), prmsFilaDerecha)
                reportesPdfService.addCellTb(tablaManoObra, new Paragraph((numero((r["rbpcpcun"] * r["rbrocntd"]), 5))?.toString(), times8normal), prmsFilaDerecha)
                reportesPdfService.addCellTb(tablaManoObra, new Paragraph(numero(r["rndm"], 5)?.toString(), times8normal), prmsFilaDerecha)
                reportesPdfService.addCellTb(tablaManoObra, new Paragraph(numero(r["parcial"], 5)?.toString(), times8normal), prmsFilaDerecha)
                totalMan += r["parcial"]
            }
        }

        reportesPdfService.addCellTb(tablaManoObra, new Paragraph("", times8bold), [border: Color.WHITE, colspan: 5])
        reportesPdfService.addCellTb(tablaManoObra, new Paragraph("TOTAL", times8bold), prmsFilaDerecha)
        reportesPdfService.addCellTb(tablaManoObra, new Paragraph(numero(totalMan, 5)?.toString(), times8bold), prmsFilaDerecha)
        reportesPdfService.addCellTb(tablaManoObra, new Paragraph("", times8bold), [border: Color.WHITE, colspan: 7])

        //MATERIALES
        PdfPTable tablaMateriales = new PdfPTable(6);
        tablaMateriales.setWidthPercentage(100);
        tablaMateriales.setWidths(arregloEnteros([8,48,9,8,10,8]))

        if(params.desglose == '0'){
            reportesPdfService.addCellTb(tablaMateriales, new Paragraph("MATERIALES INCLUIDO TRANSPORTE", times12bold), tituloRubro)
        }else{
            reportesPdfService.addCellTb(tablaMateriales, new Paragraph("MATERIALES", times12bold), tituloRubro)
        }

        reportesPdfService.addCellTb(tablaMateriales, new Paragraph("CÓDIGO", times7bold), celdaCabecera)
        reportesPdfService.addCellTb(tablaMateriales, new Paragraph("DESCRIPCIÓN", times7bold), celdaCabecera)
        reportesPdfService.addCellTb(tablaMateriales, new Paragraph("UNIDAD", times7bold), celdaCabecera)
        reportesPdfService.addCellTb(tablaMateriales, new Paragraph("CANTIDAD", times7bold), celdaCabecera)
        reportesPdfService.addCellTb(tablaMateriales, new Paragraph("UNITARIO(\$)", times7bold), celdaCabecera)
        reportesPdfService.addCellTb(tablaMateriales, new Paragraph("C.TOTAL(\$)", times7bold), celdaCabecera)

        res.eachWithIndex { r, i ->
            if (r["grpocdgo"] == 1) {
                bandMat = 1
                reportesPdfService.addCellTb(tablaMateriales, new Paragraph(r["itemcdgo"], times8normal), prmsFilaIzquierda)
                reportesPdfService.addCellTb(tablaMateriales, new Paragraph(r["itemnmbr"], times8normal), prmsFilaIzquierda)
                reportesPdfService.addCellTb(tablaMateriales, new Paragraph(r["unddcdgo"], times8normal), prmsFila)
                reportesPdfService.addCellTb(tablaMateriales, new Paragraph(numero(r["rbrocntd"], 5)?.toString(), times8normal), prmsFila)
                if (params.desglose != '0') {
                    reportesPdfService.addCellTb(tablaMateriales, new Paragraph(numero(r["rbpcpcun"], 5)?.toString(), times8normal), prmsFilaDerecha)
                    reportesPdfService.addCellTb(tablaMateriales, new Paragraph(numero(r["parcial"], 5)?.toString(), times8normal), prmsFilaDerecha)
                    totalMat += r["parcial"]
                }else{
                    reportesPdfService.addCellTb(tablaMateriales, new Paragraph(numero((r["rbpcpcun"] + r["parcial_t"] / r["rbrocntd"]), 5)?.toString(), times8normal), prmsFilaDerecha)
                    reportesPdfService.addCellTb(tablaMateriales, new Paragraph(numero((r["parcial"] + r["parcial_t"]), 5)?.toString(), times8normal), prmsFilaDerecha)
                    totalMat += (r["parcial"] + r["parcial_t"])
                }
            }
        }

        reportesPdfService.addCellTb(tablaMateriales, new Paragraph("", times8bold), [border: Color.WHITE, colspan: 4])
        reportesPdfService.addCellTb(tablaMateriales, new Paragraph("TOTAL", times8bold), prmsFilaDerecha)
        reportesPdfService.addCellTb(tablaMateriales, new Paragraph(numero(totalMat, 5)?.toString(), times8bold), prmsFilaDerecha)
        reportesPdfService.addCellTb(tablaMateriales, new Paragraph("", times8bold), [border: Color.WHITE, colspan: 6])

        //TRANSPORTE
        PdfPTable tablaTransporte = new PdfPTable(8);
        tablaTransporte.setWidthPercentage(100);
        tablaTransporte.setWidths(arregloEnteros([11,25,8,11,11,12,10,10]))

        reportesPdfService.addCellTb(tablaTransporte, new Paragraph("TRANSPORTE", times12bold), tituloRubro)

        reportesPdfService.addCellTb(tablaTransporte, new Paragraph("CÓDIGO", times7bold), celdaCabecera)
        reportesPdfService.addCellTb(tablaTransporte, new Paragraph("DESCRIPCIÓN", times7bold), celdaCabecera)
        reportesPdfService.addCellTb(tablaTransporte, new Paragraph("UNIDAD", times7bold), celdaCabecera)
        reportesPdfService.addCellTb(tablaTransporte, new Paragraph("PES/VOL", times7bold), celdaCabecera)
        reportesPdfService.addCellTb(tablaTransporte, new Paragraph("CANTIDAD", times7bold), celdaCabecera)
        reportesPdfService.addCellTb(tablaTransporte, new Paragraph("DISTANCIA", times7bold), celdaCabecera)
        reportesPdfService.addCellTb(tablaTransporte, new Paragraph("TARIFA", times7bold), celdaCabecera)
        reportesPdfService.addCellTb(tablaTransporte, new Paragraph("C.TOTAL(\$)", times7bold), celdaCabecera)

        res.eachWithIndex { r, i ->
            if (r["grpocdgo"]== 1 && params.desglose != '0') {
                reportesPdfService.addCellTb(tablaTransporte, new Paragraph(r["itemcdgo"], times8normal), prmsFilaIzquierda)
                reportesPdfService.addCellTb(tablaTransporte, new Paragraph(r["itemnmbr"], times8normal), prmsFilaIzquierda)
                if(r["tplscdgo"].trim() =='P' || r["tplscdgo"].trim() =='P1' ){
                    reportesPdfService.addCellTb(tablaTransporte, new Paragraph("ton-km", times8normal), prmsFila)
                }else{
                    if(r["tplscdgo"].trim() =='V' || r["tplscdgo"].trim() =='V1' || r["tplscdgo"].trim() =='V2') {
                        reportesPdfService.addCellTb(tablaTransporte, new Paragraph("m3-km", times8normal), prmsFila)
                    }else{
                        reportesPdfService.addCellTb(tablaTransporte, new Paragraph(r["unddcdgo"], times8normal), prmsFila)
                    }
                }
                reportesPdfService.addCellTb(tablaTransporte, new Paragraph(numero(r["itempeso"], 5)?.toString(), times8normal), prmsFila)
                reportesPdfService.addCellTb(tablaTransporte, new Paragraph(numero(r["rbrocntd"], 5)?.toString(), times8normal), prmsFila)
                reportesPdfService.addCellTb(tablaTransporte, new Paragraph(numero(r["distancia"], 5)?.toString(), times8normal), prmsFila)
                reportesPdfService.addCellTb(tablaTransporte, new Paragraph(numero(r["tarifa"], 5)?.toString(), times8normal), prmsFila)
                reportesPdfService.addCellTb(tablaTransporte, new Paragraph(numero(r["parcial_t"], 5)?.toString(), times8normal), prmsFilaDerecha)
                total += r["parcial_t"]
            }
        }

        reportesPdfService.addCellTb(tablaTransporte, new Paragraph("", times8bold), [border: Color.WHITE, colspan: 6])
        reportesPdfService.addCellTb(tablaTransporte, new Paragraph("TOTAL", times8bold), prmsFila)
        reportesPdfService.addCellTb(tablaTransporte, new Paragraph(numero(total, 5)?.toString(), times8bold), prmsFilaDerecha)
        reportesPdfService.addCellTb(tablaTransporte, new Paragraph("", times8bold), [border: Color.WHITE, colspan: 6])

        //COSTOS INDIRECTOS
        def totalRubro

//            if (params.desglose == '1') {
        totalRubro = total + totalHer + totalMan + totalMat
//            } else {
//                totalRubro = totalHer + totalMan + totalMat
//            }

        def totalIndi = totalRubro?.toDouble() * indi / 100

        PdfPTable tablaIndirectos = new PdfPTable(3);
        tablaIndirectos.setWidthPercentage(70);
        tablaIndirectos.setWidths(arregloEnteros([50,25,25]))
        tablaIndirectos.horizontalAlignment = Element.ALIGN_LEFT;

        reportesPdfService.addCellTb(tablaIndirectos, new Paragraph("COSTOS INDIRECTOS", times12bold), tituloRubro)

        reportesPdfService.addCellTb(tablaIndirectos, new Paragraph("DESCRIPCIÓN", times7bold), celdaCabecera)
        reportesPdfService.addCellTb(tablaIndirectos, new Paragraph("PORCENTAJE", times7bold), celdaCabecera)
        reportesPdfService.addCellTb(tablaIndirectos, new Paragraph("VALOR", times7bold), celdaCabecera)

        reportesPdfService.addCellTb(tablaIndirectos, new Paragraph("COSTOS INDIRECTOS", times8normal), prmsFilaIzquierda)
        reportesPdfService.addCellTb(tablaIndirectos, new Paragraph(numero(indi, 1)?.toString() + "%", times8normal), prmsFila)
        reportesPdfService.addCellTb(tablaIndirectos, new Paragraph(numero(totalIndi, 5)?.toString(), times8normal), prmsFila)
        reportesPdfService.addCellTb(tablaIndirectos, new Paragraph("", times8bold), [border: Color.WHITE, colspan: 3])

        PdfPTable tablaTotales = new PdfPTable(2);
        tablaTotales.setWidthPercentage(40);
        tablaTotales.setWidths(arregloEnteros([50,25]))
        tablaTotales.horizontalAlignment = Element.ALIGN_RIGHT;

        reportesPdfService.addCellTb(tablaTotales, new Paragraph("COSTO UNITARIO DIRECTO", times8bold), celdaCabeceraIzquierda)
        reportesPdfService.addCellTb(tablaTotales, new Paragraph(numero(totalRubro, 2)?.toString(), times8bold), celdaCabeceraDerecha)

        reportesPdfService.addCellTb(tablaTotales, new Paragraph("COSTOS INDIRECTO", times8bold), prmsFilaIzquierda)
        reportesPdfService.addCellTb(tablaTotales, new Paragraph(numero(totalIndi, 2)?.toString(), times8bold), prmsFilaDerecha)

        reportesPdfService.addCellTb(tablaTotales, new Paragraph("COSTO TOTAL DEL RUBRO", times8bold), prmsFilaIzquierda)
        reportesPdfService.addCellTb(tablaTotales, new Paragraph(numero((totalRubro + totalIndi), 2)?.toString(), times8bold), prmsFilaDerecha)

        reportesPdfService.addCellTb(tablaTotales, new Paragraph("PRECIO UNITARIO \$USD", times8bold), celdaCabeceraIzquierda2)
        reportesPdfService.addCellTb(tablaTotales, new Paragraph(numero((totalRubro + totalIndi), 2)?.toString(), times8bold), celdaCabeceraDerecha2)

        PdfPTable tablaNota = new PdfPTable(2);
        tablaNota.setWidthPercentage(100);
        tablaNota.setWidths(arregloEnteros([6, 94]))
        if(rubro?.codigo?.split('-')[0] == 'TR'){
            reportesPdfService.addCellTb(tablaNota, new Paragraph("Distancia a la escombrera:", times8bold), prmsFilaIzquierda)
            reportesPdfService.addCellTb(tablaNota, new Paragraph("${obra?.distanciaDesalojo}" + "km", times8normal), prmsFilaIzquierda)
        }
        reportesPdfService.addCellTb(tablaNota, new Paragraph("Nota:", times8bold), prmsFilaIzquierda)
        reportesPdfService.addCellTb(tablaNota, new Paragraph("Los cálculos se hacen con todos los " +
                "decimales y el resultado final se lo redondea a dos decimales.", times8normal), prmsFilaIzquierda)

        document.add(headers)
        document.add(tablaCoeficiente)
        document.add(tablaEquipos)
        document.add(tablaManoObra)
        document.add(tablaMateriales)
        if(params.desglose != '0'){
            document.add(tablaTransporte)
        }
        document.add(tablaIndirectos)
        document.add(tablaTotales)
        document.add(tablaNota)

        document.close();
        pdfw.close()
        byte[] b = baos.toByteArray();
        response.setContentType("application/pdf")
        response.setHeader("Content-disposition", "attachment; filename=" + name)
        response.setContentLength(b.length)
        response.getOutputStream().write(b)
    }

    def reporteRubrosVaeVolumen(){
        println("params rrvv " + params)

        def obra = Obra.get(params.obra)

        def fecha1
        def fecha2

        if(params.fecha){
            fecha1 = new Date().parse("dd-MM-yyyy", params.fecha)
        }else {
        }

        if(params.fechaSalida){
            fecha2 = new Date().parse("dd-MM-yyyy", params.fechaSalida)
        }else {
        }

        def vol1 = VolumenesObra.get(params.id)
        def rubro = Item.get(vol1.item.id)
        def indi = obra.totales

        try {
            indi = indi.toDouble()
        } catch (e) {
            println "error parse " + e
            indi = 21.5
        }

        preciosService.ac_rbroObra(obra.id)
        def res = preciosService.precioUnitarioVolumenObraAsc("*", obra.id, rubro.id)
        def vae = preciosService.vae_rb(obra.id,rubro.id)

        def bandMat = 0
        def band = 0
        def bandTrans = params.desglose

        def prmsHeaderHoja = [border: Color.WHITE]
        def prmsFila = [border: Color.WHITE, align : Element.ALIGN_CENTER, valign: Element.ALIGN_MIDDLE]
        def prmsFilaIzquierda = [border: Color.WHITE, align : Element.ALIGN_LEFT, valign: Element.ALIGN_MIDDLE]
        def prmsFilaDerecha = [border: Color.WHITE, align : Element.ALIGN_RIGHT, valign: Element.ALIGN_MIDDLE]
        def prmsHeaderHoja2 = [border: Color.WHITE, colspan: 9]
        def prmsHeader = [border: Color.WHITE, colspan: 7, bg: new Color(73, 175, 205),
                          align : Element.ALIGN_CENTER, valign: Element.ALIGN_MIDDLE]
        def prmsHeader2 = [border: Color.WHITE, colspan: 3, bg: new Color(73, 175, 205),
                           align : Element.ALIGN_CENTER, valign: Element.ALIGN_MIDDLE]
        def prmsCellHead = [border: Color.WHITE, bg: new Color(73, 175, 205),
                            align : Element.ALIGN_CENTER, valign: Element.ALIGN_MIDDLE]
        def prmsCellCenter = [border: Color.BLACK, align: Element.ALIGN_CENTER, valign: Element.ALIGN_MIDDLE]
        def prmsCellRight = [border: Color.BLACK, align: Element.ALIGN_RIGHT, valign: Element.ALIGN_MIDDLE]
        def prmsCellLeft = [border: Color.BLACK, valign: Element.ALIGN_MIDDLE]
        def prmsSubtotal = [border: Color.BLACK, colspan: 6,
                            align : Element.ALIGN_RIGHT, valign: Element.ALIGN_MIDDLE]
        def prmsNum = [border: Color.BLACK, align: Element.ALIGN_RIGHT, valign: Element.ALIGN_MIDDLE]

        def celdaCabecera = [border: Color.BLACK, bg: new Color(220, 220, 220), align: Element.ALIGN_CENTER, valign: Element.ALIGN_MIDDLE, bordeBot: "1"]
        def celdaCabeceraIzquierda = [bct: Color.BLACK, bcl: Color.WHITE, bcr:Color.WHITE, bcb: Color.WHITE, align: Element.ALIGN_LEFT, valign: Element.ALIGN_MIDDLE]
        def celdaCabeceraDerecha = [bct: Color.BLACK, bcl: Color.WHITE, bcr:Color.WHITE, bcb: Color.WHITE, align: Element.ALIGN_RIGHT, valign: Element.ALIGN_MIDDLE]
        def celdaCabeceraCentro = [bct: Color.BLACK, bcl: Color.WHITE, bcr:Color.WHITE, bcb: Color.WHITE, align: Element.ALIGN_CENTER, valign: Element.ALIGN_MIDDLE]
        def celdaCabeceraCentro2 = [bcb: Color.BLACK, bcl: Color.WHITE, bcr:Color.WHITE, bct: Color.WHITE, align: Element.ALIGN_CENTER, valign: Element.ALIGN_MIDDLE]
        def celdaCabeceraDerecha2 = [bcb: Color.BLACK, bcl: Color.WHITE, bcr:Color.WHITE, bct: Color.WHITE, align: Element.ALIGN_RIGHT, valign: Element.ALIGN_MIDDLE]
        def celdaCabeceraIzquierda2 = [bcb: Color.BLACK, bcl: Color.WHITE, bcr:Color.WHITE, bct: Color.WHITE, align: Element.ALIGN_LEFT, valign: Element.ALIGN_MIDDLE]

        def tituloRubro = [height: 20, border: Color.WHITE, colspan: 12, align : Element.ALIGN_LEFT, valign: Element.ALIGN_TOP]
        def tituloRubro13 = [height: 20, border: Color.WHITE, colspan: 13, align : Element.ALIGN_LEFT, valign: Element.ALIGN_TOP]
        def tituloRubro3 = [height: 20, border: Color.WHITE, colspan: 3, align : Element.ALIGN_LEFT, valign: Element.ALIGN_TOP]

        def prms = [prmsHeaderHoja: prmsHeaderHoja, prmsHeader: prmsHeader, prmsHeader2: prmsHeader2,
                    prmsCellHead: prmsCellHead, prmsCell: prmsCellCenter, prmsCellLeft: prmsCellLeft, prmsSubtotal: prmsSubtotal, prmsNum: prmsNum, prmsHeaderHoja2: prmsHeaderHoja2, prmsCellRight: prmsCellRight]

        Font times12bold = new Font(Font.TIMES_ROMAN, 12, Font.BOLD)
        Font times14bold = new Font(Font.TIMES_ROMAN, 14, Font.BOLD)
        Font times10bold = new Font(Font.TIMES_ROMAN, 10, Font.BOLD)
        Font times10normal = new Font(Font.TIMES_ROMAN, 10, Font.NORMAL)
        Font times8bold = new Font(Font.TIMES_ROMAN, 8, Font.BOLD)
        Font times8normal = new Font(Font.TIMES_ROMAN, 8, Font.NORMAL)
        Font times7bold = new Font(Font.TIMES_ROMAN, 7, Font.BOLD)
        Font times7normal = new Font(Font.TIMES_ROMAN, 7, Font.NORMAL)
        Font times10boldWhite = new Font(Font.TIMES_ROMAN, 10, Font.BOLD);
        Font times8boldWhite = new Font(Font.TIMES_ROMAN, 8, Font.BOLD)
        times8boldWhite.setColor(Color.WHITE)
        times10boldWhite.setColor(Color.WHITE)

        def name = ''
        if(params.desglose == '0'){
            name = "reporteRubrosVaeSinDesglose_" + new Date().format("ddMMyyyy_hhmm") + ".pdf";
        }else{
            name = "reporteRubrosVaeConDesglose_" + new Date().format("ddMMyyyy_hhmm") + ".pdf";
        }

        def baos = new ByteArrayOutputStream()
        Document document
        document = new Document(PageSize.A4.rotate())
        document.setMargins(60, 24, 45, 45);

        def pdfw = PdfWriter.getInstance(document, baos);
        document.open();
        document.addTitle("Rubros " + new Date().format("dd_MM_yyyy"));
        document.addSubject("Generado por el sistema Obras");
        document.addKeywords("documentosObra, janus, rubros");
        document.addAuthor("OBRAS");
        document.addCreator("Tedein SA");

        def nombre = rubro?.nombre
        def total = 0, totalHer = 0, totalMan = 0, totalMat = 0, totalHerRel = 0,
            totalHerVae = 0, totalManRel = 0, totalManVae = 0, totalMatRel = 0, totalMatVae = 0,
            totalTRel=0, totalTVae=0

        Paragraph headers = new Paragraph();
        addEmptyLine(headers, 1);
        headers.setAlignment(Element.ALIGN_CENTER);
        headers.add(new Paragraph("G.A.D. LOS RÍOS", times14bold));
        headers.add(new Paragraph("COORDINACIÓN DE FIJACIÓN DE PRECIOS UNITARIOS - ANÁLISIS DE PRECIOS UNITARIOS", times10bold));
        headers.add(new Paragraph("", times14bold));

        PdfPTable tablaCoeficiente = new PdfPTable(6);
        tablaCoeficiente.setWidthPercentage(100);
        tablaCoeficiente.setWidths(arregloEnteros([15,18, 15,18, 15,18]))

        reportesPdfService.addCellTb(tablaCoeficiente, new Paragraph("Fecha: ", times10bold), prmsHeaderHoja)
        reportesPdfService.addCellTb(tablaCoeficiente, new Paragraph((fecha2?.format("dd-MM-yyyy") ?: ''), times10normal), [border: Color.WHITE, colspan: 3])
        reportesPdfService.addCellTb(tablaCoeficiente, new Paragraph("Fecha Act. P.U: ", times10bold), prmsHeaderHoja)
        reportesPdfService.addCellTb(tablaCoeficiente, new Paragraph((fecha1?.format("dd-MM-yyyy") ?: '') , times10normal), prmsHeaderHoja)

        reportesPdfService.addCellTb(tablaCoeficiente, new Paragraph("Código de obra: ", times10bold), prmsHeaderHoja)
        reportesPdfService.addCellTb(tablaCoeficiente, new Paragraph((obra?.codigo ?: ''), times10normal), [border: Color.WHITE, colspan: 5])

        reportesPdfService.addCellTb(tablaCoeficiente, new Paragraph("Presupuesto: ", times10bold), prmsHeaderHoja)
        reportesPdfService.addCellTb(tablaCoeficiente, new Paragraph((obra?.nombre ?: ''), times10normal), [border: Color.WHITE, colspan: 5])

        reportesPdfService.addCellTb(tablaCoeficiente, new Paragraph("Código de rubro: ", times10bold), prmsHeaderHoja)
        reportesPdfService.addCellTb(tablaCoeficiente, new Paragraph((rubro?.codigo ?: ''), times10normal), prmsHeaderHoja)
        reportesPdfService.addCellTb(tablaCoeficiente, new Paragraph("Código de especificación: ", times10bold), prmsHeaderHoja)
        reportesPdfService.addCellTb(tablaCoeficiente, new Paragraph((rubro?.codigoEspecificacion ?: ''), times10normal), prmsHeaderHoja)
        reportesPdfService.addCellTb(tablaCoeficiente, new Paragraph("Unidad: ", times10bold), prmsHeaderHoja)
        reportesPdfService.addCellTb(tablaCoeficiente, new Paragraph((rubro?.unidad?.codigo ?: ''), times10normal), prmsHeaderHoja)

        reportesPdfService.addCellTb(tablaCoeficiente, new Paragraph("Descripción: ", times10bold), prmsHeaderHoja)
        reportesPdfService.addCellTb(tablaCoeficiente, new Paragraph((nombre ?: ''), times10normal), [border: Color.WHITE, colspan: 5])

        //EQUIPOS
        PdfPTable tablaEquipos = new PdfPTable(12);
        tablaEquipos.setWidthPercentage(100);
        tablaEquipos.setWidths(arregloEnteros([8,30,7,6,6,9,7,7,7,5,4,8]))

        reportesPdfService.addCellTb(tablaEquipos, new Paragraph("EQUIPOS", times12bold), tituloRubro)

        reportesPdfService.addCellTb(tablaEquipos, new Paragraph("CÓDIGO", times7bold), celdaCabecera)
        reportesPdfService.addCellTb(tablaEquipos, new Paragraph("DESCRIPCIÓN", times7bold), celdaCabecera)
        reportesPdfService.addCellTb(tablaEquipos, new Paragraph("CANTIDAD", times7bold), celdaCabecera)
        reportesPdfService.addCellTb(tablaEquipos, new Paragraph("TARIFA (\$/H)", times7bold), celdaCabecera)
        reportesPdfService.addCellTb(tablaEquipos, new Paragraph("COSTOS (\$)", times7bold), celdaCabecera)
        reportesPdfService.addCellTb(tablaEquipos, new Paragraph("RENDIMIENTO", times7bold), celdaCabecera)
        reportesPdfService.addCellTb(tablaEquipos, new Paragraph("C.TOTAL(\$)", times7bold), celdaCabecera)
        reportesPdfService.addCellTb(tablaEquipos, new Paragraph("PESO RELAT(%)", times7bold), celdaCabecera)
        reportesPdfService.addCellTb(tablaEquipos, new Paragraph("CPC", times7bold), celdaCabecera)
        reportesPdfService.addCellTb(tablaEquipos, new Paragraph("NP/EP/ ND", times7bold), celdaCabecera)
        reportesPdfService.addCellTb(tablaEquipos, new Paragraph("VAE (%)", times7bold), celdaCabecera)
        reportesPdfService.addCellTb(tablaEquipos, new Paragraph("VAE(%) ELEMENTO", times7bold), celdaCabecera)

        vae.eachWithIndex { r, i ->
            if (r["grpocdgo"] == 3) {
                reportesPdfService.addCellTb(tablaEquipos, new Paragraph(r["itemcdgo"], times8normal), prmsFilaIzquierda)
                reportesPdfService.addCellTb(tablaEquipos, new Paragraph(r["itemnmbr"], times8normal), prmsFilaIzquierda)
                reportesPdfService.addCellTb(tablaEquipos, new Paragraph(numero(r["rbrocntd"], 5)?.toString(), times8normal), prmsFila)
                reportesPdfService.addCellTb(tablaEquipos, new Paragraph(numero(r["rbpcpcun"], 5)?.toString(), times8normal), prmsFilaDerecha)
                reportesPdfService.addCellTb(tablaEquipos, new Paragraph((numero((r["rbpcpcun"] * r["rbrocntd"]), 5))?.toString(), times8normal), prmsFilaDerecha)
                reportesPdfService.addCellTb(tablaEquipos, new Paragraph(numero(r["rndm"], 5)?.toString(), times8normal), prmsFila)
                reportesPdfService.addCellTb(tablaEquipos, new Paragraph(numero(r["parcial"], 5)?.toString(), times8normal), prmsFilaDerecha)
                reportesPdfService.addCellTb(tablaEquipos, new Paragraph(numero(r["relativo"], 2)?.toString(), times8normal), prmsFila)
                reportesPdfService.addCellTb(tablaEquipos, new Paragraph(r["itemcpac"]?.toString(), times8normal), prmsFila)
                reportesPdfService.addCellTb(tablaEquipos, new Paragraph(r["tpbncdgo"], times8normal), prmsFila)
                reportesPdfService.addCellTb(tablaEquipos, new Paragraph(numero(r["vae"], 2)?.toString(), times8normal), prmsFila)
                reportesPdfService.addCellTb(tablaEquipos, new Paragraph((numero(r["vae_vlor"],2))?.toString(), times8normal), prmsFila)
                totalHer += r["parcial"]
                totalHerRel += r["relativo"]
                totalHerVae += r["vae_vlor"]
            }
        }

        reportesPdfService.addCellTb(tablaEquipos, new Paragraph("", times8bold), [border: Color.WHITE, colspan: 5])
        reportesPdfService.addCellTb(tablaEquipos, new Paragraph("TOTAL", times8bold), prmsFila)
        reportesPdfService.addCellTb(tablaEquipos, new Paragraph(numero(totalHer, 5)?.toString(), times8bold), prmsFilaDerecha)
        reportesPdfService.addCellTb(tablaEquipos, new Paragraph(numero(totalHerRel, 2)?.toString(), times8bold), prmsFila)
        reportesPdfService.addCellTb(tablaEquipos, new Paragraph("", times8bold), [border: Color.WHITE, colspan: 3])
        reportesPdfService.addCellTb(tablaEquipos, new Paragraph(numero(totalHerVae, 2)?.toString(), times8bold), prmsFila)

        //MANO DE OBRA
        PdfPTable tablaManoObra = new PdfPTable(12);
        tablaManoObra.setWidthPercentage(100);
        tablaManoObra.setWidths(arregloEnteros([6,32,7,6,6,9,7,7,7,5,4,8]))

        reportesPdfService.addCellTb(tablaManoObra, new Paragraph("MANO DE OBRA", times12bold), tituloRubro)

        reportesPdfService.addCellTb(tablaManoObra, new Paragraph("CÓDIGO", times7bold), celdaCabecera)
        reportesPdfService.addCellTb(tablaManoObra, new Paragraph("DESCRIPCIÓN", times7bold), celdaCabecera)
        reportesPdfService.addCellTb(tablaManoObra, new Paragraph("CANTIDAD", times7bold), celdaCabecera)
        reportesPdfService.addCellTb(tablaManoObra, new Paragraph("JORNAL (\$/H)", times7bold), celdaCabecera)
        reportesPdfService.addCellTb(tablaManoObra, new Paragraph("COSTOS (\$)", times7bold), celdaCabecera)
        reportesPdfService.addCellTb(tablaManoObra, new Paragraph("RENDIMIENTO", times7bold), celdaCabecera)
        reportesPdfService.addCellTb(tablaManoObra, new Paragraph("C.TOTAL (\$)", times7bold), celdaCabecera)
        reportesPdfService.addCellTb(tablaManoObra, new Paragraph("PESO RELAT(%)", times7bold), celdaCabecera)
        reportesPdfService.addCellTb(tablaManoObra, new Paragraph("CPC", times7bold), celdaCabecera)
        reportesPdfService.addCellTb(tablaManoObra, new Paragraph("NP/EP/ ND", times7bold), celdaCabecera)
        reportesPdfService.addCellTb(tablaManoObra, new Paragraph("VAE (%)", times7bold), celdaCabecera)
        reportesPdfService.addCellTb(tablaManoObra, new Paragraph("VAE(%) ELEMENTO", times7bold), celdaCabecera)

        vae.eachWithIndex { r, i ->
            if (r["grpocdgo"] == 2) {
                reportesPdfService.addCellTb(tablaManoObra, new Paragraph(r["itemcdgo"], times8normal), prmsFilaIzquierda)
                reportesPdfService.addCellTb(tablaManoObra, new Paragraph(r["itemnmbr"], times8normal), prmsFilaIzquierda)
                reportesPdfService.addCellTb(tablaManoObra, new Paragraph(numero(r["rbrocntd"], 5)?.toString(), times8normal), prmsFila)
                reportesPdfService.addCellTb(tablaManoObra, new Paragraph(numero(r["rbpcpcun"], 5)?.toString(), times8normal), prmsFilaDerecha)
                reportesPdfService.addCellTb(tablaManoObra, new Paragraph((numero((r["rbpcpcun"] * r["rbrocntd"]), 5))?.toString(), times8normal), prmsFilaDerecha)
                reportesPdfService.addCellTb(tablaManoObra, new Paragraph(numero(r["rndm"], 5)?.toString(), times8normal), prmsFila)
                reportesPdfService.addCellTb(tablaManoObra, new Paragraph(numero(r["parcial"], 5)?.toString(), times8normal), prmsFilaDerecha)
                reportesPdfService.addCellTb(tablaManoObra, new Paragraph(numero(r["relativo"], 2)?.toString(), times8normal), prmsFila)
                reportesPdfService.addCellTb(tablaManoObra, new Paragraph(r["itemcpac"]?.toString(), times8normal), prmsFila)
                reportesPdfService.addCellTb(tablaManoObra, new Paragraph(r["tpbncdgo"], times8normal), prmsFila)
                reportesPdfService.addCellTb(tablaManoObra, new Paragraph(numero(r["vae"], 2)?.toString(), times8normal), prmsFila)
                reportesPdfService.addCellTb(tablaManoObra, new Paragraph((numero(r["vae_vlor"],2))?.toString(), times8normal), prmsFila)
                totalMan += r["parcial"]
                totalManRel += r["relativo"]
                totalManVae += r["vae_vlor"]
            }
        }

        reportesPdfService.addCellTb(tablaManoObra, new Paragraph("", times8bold), [border: Color.WHITE, colspan: 5])
        reportesPdfService.addCellTb(tablaManoObra, new Paragraph("TOTAL", times8bold), prmsFila)
        reportesPdfService.addCellTb(tablaManoObra, new Paragraph(numero(totalMan, 5)?.toString(), times8bold), prmsFilaDerecha)
        reportesPdfService.addCellTb(tablaManoObra, new Paragraph(numero(totalManRel, 2)?.toString(), times8bold), prmsFila)
        reportesPdfService.addCellTb(tablaManoObra, new Paragraph("", times14bold), [border: Color.WHITE, colspan: 3])
        reportesPdfService.addCellTb(tablaManoObra, new Paragraph(numero(totalManVae, 2)?.toString(), times8bold), prmsFila)


        //MATERIALES
        PdfPTable tablaMateriales = new PdfPTable(11);
        tablaMateriales.setWidthPercentage(100);
        tablaMateriales.setWidths(arregloEnteros([8,37, 6,6,9,7,7,7,5,4,8]))

        if(params.desglose == '0'){
            reportesPdfService.addCellTb(tablaMateriales, new Paragraph("MATERIALES INCLUIDO TRANSPORTE", times12bold), tituloRubro)
        }else{
            reportesPdfService.addCellTb(tablaMateriales, new Paragraph("MATERIALES", times12bold), tituloRubro)
        }

        reportesPdfService.addCellTb(tablaMateriales, new Paragraph("CÓDIGO", times7bold), celdaCabecera)
        reportesPdfService.addCellTb(tablaMateriales, new Paragraph("DESCRIPCIÓN", times7bold), celdaCabecera)
        reportesPdfService.addCellTb(tablaMateriales, new Paragraph("UNIDAD", times7bold), celdaCabecera)
        reportesPdfService.addCellTb(tablaMateriales, new Paragraph("CANTI- DAD", times7bold), celdaCabecera)
        reportesPdfService.addCellTb(tablaMateriales, new Paragraph("UNITARIO(\$)", times7bold), celdaCabecera)
        reportesPdfService.addCellTb(tablaMateriales, new Paragraph("C.TOTAL(\$)", times7bold), celdaCabecera)
        reportesPdfService.addCellTb(tablaMateriales, new Paragraph("PESO RELAT(%)", times7bold), celdaCabecera)
        reportesPdfService.addCellTb(tablaMateriales, new Paragraph("CPC", times7bold), celdaCabecera)
        reportesPdfService.addCellTb(tablaMateriales, new Paragraph("NP/EP/ND", times7bold), celdaCabecera)
        reportesPdfService.addCellTb(tablaMateriales, new Paragraph("VAE(%)", times7bold), celdaCabecera)
        reportesPdfService.addCellTb(tablaMateriales, new Paragraph("VAE(%) ELEMENTO", times7bold), celdaCabecera)

        vae.eachWithIndex { r, i ->
            if (r["grpocdgo"] == 1) {
                bandMat = 1
                reportesPdfService.addCellTb(tablaMateriales, new Paragraph(r["itemcdgo"], times8normal), prmsFilaIzquierda)
                reportesPdfService.addCellTb(tablaMateriales, new Paragraph(r["itemnmbr"], times8normal), prmsFilaIzquierda)
                reportesPdfService.addCellTb(tablaMateriales, new Paragraph(r["unddcdgo"], times8normal), prmsFila)
                reportesPdfService.addCellTb(tablaMateriales, new Paragraph(numero(r["rbrocntd"], 5)?.toString(), times8normal), prmsFila)
                if (params.desglose != '0') {
                    reportesPdfService.addCellTb(tablaMateriales, new Paragraph(numero(r["rbpcpcun"], 5)?.toString(), times8normal), prmsFilaDerecha)
                    reportesPdfService.addCellTb(tablaMateriales, new Paragraph(numero(r["parcial"], 5)?.toString(), times8normal), prmsFilaDerecha)
                    reportesPdfService.addCellTb(tablaMateriales, new Paragraph(numero(r["relativo"], 2)?.toString(), times8normal), prmsFila)
                    reportesPdfService.addCellTb(tablaMateriales, new Paragraph(r["itemcpac"]?.toString(), times8normal), prmsFila)
                    reportesPdfService.addCellTb(tablaMateriales, new Paragraph(r["tpbncdgo"], times8normal), prmsFila)
                    reportesPdfService.addCellTb(tablaMateriales, new Paragraph(numero(r["vae"], 2)?.toString(), times8normal), prmsFila)
                    reportesPdfService.addCellTb(tablaMateriales, new Paragraph((numero(r["vae_vlor"],2))?.toString(), times8normal), prmsFila)

                    totalMat += r["parcial"]
                    totalMatRel += r["relativo"]
                    totalMatVae += r["vae_vlor"]
                }else{
                    reportesPdfService.addCellTb(tablaMateriales, new Paragraph(numero((r["rbpcpcun"] + r["parcial_t"] / r["rbrocntd"]), 5)?.toString(), times8normal), prmsFilaDerecha)
                    reportesPdfService.addCellTb(tablaMateriales, new Paragraph(numero((r["parcial"] + r["parcial_t"]), 5)?.toString(), times8normal), prmsFilaDerecha)
                    reportesPdfService.addCellTb(tablaMateriales, new Paragraph(numero((r["relativo"] + r["relativo_t"]), 2)?.toString(), times8normal), prmsFila)
                    reportesPdfService.addCellTb(tablaMateriales, new Paragraph(r["itemcpac"]?.toString(), times8normal), prmsFila)
                    reportesPdfService.addCellTb(tablaMateriales, new Paragraph(r["tpbncdgo"], times8normal), prmsFila)
                    reportesPdfService.addCellTb(tablaMateriales, new Paragraph(numero(r["vae"], 2)?.toString(), times8normal), prmsFila)
                    reportesPdfService.addCellTb(tablaMateriales, new Paragraph((numero(r["vae_vlor"] + r["vae_vlor_t"],2))?.toString(), times8normal), prmsFila)

                    totalMat += (r["parcial"] + r["parcial_t"])
                    totalMatRel += (r["relativo"] + r["relativo_t"])
                    totalMatVae += (r["vae_vlor"] + r["vae_vlor_t"])
                }

            }
        }

        reportesPdfService.addCellTb(tablaMateriales, new Paragraph("", times8bold), [border: Color.WHITE, colspan: 4])
        reportesPdfService.addCellTb(tablaMateriales, new Paragraph("TOTAL", times8bold), prmsFila)
        reportesPdfService.addCellTb(tablaMateriales, new Paragraph(numero(totalMat, 5)?.toString(), times8bold), prmsFilaDerecha)
        reportesPdfService.addCellTb(tablaMateriales, new Paragraph(numero(totalMatRel, 2)?.toString(), times8bold), prmsFila)
        reportesPdfService.addCellTb(tablaMateriales, new Paragraph("", times8bold), [border: Color.WHITE, colspan: 3])
        reportesPdfService.addCellTb(tablaMateriales, new Paragraph(numero(totalMatVae, 2)?.toString(), times8bold), prmsFila)

        //TRANSPORTE
        PdfPTable tablaTransporte = new PdfPTable(13);
        tablaTransporte.setWidthPercentage(100);
        tablaTransporte.setWidths(arregloEnteros([8,27,4,6,6,6,9,7,7,7,5,4,8]))

        reportesPdfService.addCellTb(tablaTransporte, new Paragraph("TRANSPORTE", times12bold), tituloRubro13)

        reportesPdfService.addCellTb(tablaTransporte, new Paragraph("CÓDIGO", times7bold), celdaCabecera)
        reportesPdfService.addCellTb(tablaTransporte, new Paragraph("DESCRIPCIÓN", times7bold), celdaCabecera)
        reportesPdfService.addCellTb(tablaTransporte, new Paragraph("UNI- DAD", times7bold), celdaCabecera)
        reportesPdfService.addCellTb(tablaTransporte, new Paragraph("PES/VOL", times7bold), celdaCabecera)
        reportesPdfService.addCellTb(tablaTransporte, new Paragraph("CANT.", times7bold), celdaCabecera)
        reportesPdfService.addCellTb(tablaTransporte, new Paragraph("DISTAN- CIA", times7bold), celdaCabecera)
        reportesPdfService.addCellTb(tablaTransporte, new Paragraph("TARIFA", times7bold), celdaCabecera)
        reportesPdfService.addCellTb(tablaTransporte, new Paragraph("C.TOTAL(\$)", times7bold), celdaCabecera)
        reportesPdfService.addCellTb(tablaTransporte, new Paragraph("PESO RELAT(%)", times7bold), celdaCabecera)
        reportesPdfService.addCellTb(tablaTransporte, new Paragraph("CPC", times7bold), celdaCabecera)
        reportesPdfService.addCellTb(tablaTransporte, new Paragraph("NP/EP/ ND", times7bold), celdaCabecera)
        reportesPdfService.addCellTb(tablaTransporte, new Paragraph("VAE(%)", times7bold), celdaCabecera)
        reportesPdfService.addCellTb(tablaTransporte, new Paragraph("VAE(%) ELEMENTO", times7bold), celdaCabecera)

        vae.eachWithIndex { r, i ->
            if (r["grpocdgo"]== 1 && params.desglose != '0') {
                reportesPdfService.addCellTb(tablaTransporte, new Paragraph(r["itemcdgo"], times8normal), prmsFilaIzquierda)
                reportesPdfService.addCellTb(tablaTransporte, new Paragraph(r["itemnmbr"], times8normal), prmsFilaIzquierda)
                if(r["tplscdgo"].trim() =='P' || r["tplscdgo"].trim() =='P1' ){
                    reportesPdfService.addCellTb(tablaTransporte, new Paragraph("ton-km", times8normal), prmsFila)
                }else{
                    if(r["tplscdgo"].trim() =='V' || r["tplscdgo"].trim() =='V1' || r["tplscdgo"].trim() =='V2') {
                        reportesPdfService.addCellTb(tablaTransporte, new Paragraph("m3-km", times8normal), prmsFila)
                    }else{
                        reportesPdfService.addCellTb(tablaTransporte, new Paragraph(r["unddcdgo"], times8normal), prmsFila)
                    }
                }
                reportesPdfService.addCellTb(tablaTransporte, new Paragraph(numero(r["itempeso"], 5)?.toString(), times8normal), prmsFila)
                reportesPdfService.addCellTb(tablaTransporte, new Paragraph(numero(r["rbrocntd"], 5)?.toString(), times8normal), prmsFila)
                reportesPdfService.addCellTb(tablaTransporte, new Paragraph(numero(r["distancia"], 5)?.toString(), times8normal), prmsFila)
                reportesPdfService.addCellTb(tablaTransporte, new Paragraph(numero(r["tarifa"], 5)?.toString(), times8normal), prmsFila)
                reportesPdfService.addCellTb(tablaTransporte, new Paragraph(numero(r["parcial_t"], 5)?.toString(), times8normal), prmsFilaDerecha)
                reportesPdfService.addCellTb(tablaTransporte, new Paragraph(numero(r["relativo_t"], 2)?.toString(), times8normal), prmsFilaDerecha)
                reportesPdfService.addCellTb(tablaTransporte, new Paragraph((r["itemcpac"] ?: '')?.toString(), times8normal), prmsFila)
                reportesPdfService.addCellTb(tablaTransporte, new Paragraph(r["tpbncdgo"], times8normal), prmsFila)
                reportesPdfService.addCellTb(tablaTransporte, new Paragraph(numero(r["vae_t"], 2)?.toString(), times8normal), prmsFila)
                reportesPdfService.addCellTb(tablaTransporte, new Paragraph(numero(r["vae_vlor_t"], 2)?.toString(), times8normal), prmsFila)
                total += r["parcial_t"]
                totalTRel += r["relativo_t"]
                totalTVae += r["vae_vlor_t"]
            }
        }

        reportesPdfService.addCellTb(tablaTransporte, new Paragraph("", times8bold), [border: Color.WHITE, colspan: 6])
        reportesPdfService.addCellTb(tablaTransporte, new Paragraph("TOTAL", times8bold), prmsFila)
        reportesPdfService.addCellTb(tablaTransporte, new Paragraph(numero(total, 5)?.toString(), times8bold), prmsFilaDerecha)
        reportesPdfService.addCellTb(tablaTransporte, new Paragraph(numero(totalTRel, 2)?.toString(), times8bold), prmsFilaDerecha)
        reportesPdfService.addCellTb(tablaTransporte, new Paragraph("", times14bold), [border: Color.WHITE, colspan: 3])
        reportesPdfService.addCellTb(tablaTransporte, new Paragraph(numero(totalTVae, 2)?.toString(), times8bold), prmsFila)


        //COSTOS INDIRECTOS
        def totalRubro = total + totalHer + totalMan + totalMat
        def totalRelativo = totalTRel + totalHerRel + totalMatRel + totalManRel
        def totalVae = totalTVae + totalHerVae + totalMatVae + totalManVae
        def totalIndi = totalRubro?.toDouble() * indi / 100

        PdfPTable tablaIndirectos = new PdfPTable(3);
        tablaIndirectos.setWidthPercentage(70);
        tablaIndirectos.setWidths(arregloEnteros([50,25,25]))
        tablaIndirectos.horizontalAlignment = Element.ALIGN_LEFT;

        reportesPdfService.addCellTb(tablaIndirectos, new Paragraph("COSTOS INDIRECTOS",  times12bold), tituloRubro3)

        reportesPdfService.addCellTb(tablaIndirectos, new Paragraph("DESCRIPCIÓN", times8bold), celdaCabecera)
        reportesPdfService.addCellTb(tablaIndirectos, new Paragraph("PORCENTAJE", times8bold), celdaCabecera)
        reportesPdfService.addCellTb(tablaIndirectos, new Paragraph("VALOR", times8bold), celdaCabecera)

        reportesPdfService.addCellTb(tablaIndirectos, new Paragraph("COSTOS INDIRECTOS", times8normal), prmsFilaIzquierda)
        reportesPdfService.addCellTb(tablaIndirectos, new Paragraph(numero(indi, 1)?.toString() + "%", times8normal), prmsFila)
        reportesPdfService.addCellTb(tablaIndirectos, new Paragraph(numero(totalIndi, 5)?.toString(), times8normal), prmsFila)

        reportesPdfService.addCellTb(tablaIndirectos, new Paragraph("", times8bold), [border: Color.WHITE, colspan: 3])
        reportesPdfService.addCellTb(tablaIndirectos, new Paragraph("", times8bold), [border: Color.WHITE, colspan: 3])

        PdfPTable tablaTotales = new PdfPTable(4);
        tablaTotales.setWidthPercentage(70);
        tablaTotales.setWidths(arregloEnteros([30,25,25,20]))
        tablaTotales.horizontalAlignment = Element.ALIGN_RIGHT;

        reportesPdfService.addCellTb(tablaTotales, new Paragraph("COSTO UNITARIO DIRECTO", times8bold), celdaCabeceraIzquierda)
        reportesPdfService.addCellTb(tablaTotales, new Paragraph(numero(totalRubro, 2)?.toString(), times8bold), celdaCabeceraDerecha)
        reportesPdfService.addCellTb(tablaTotales, new Paragraph(numero(totalRelativo, 2)?.toString(), times8bold), celdaCabeceraCentro)
        reportesPdfService.addCellTb(tablaTotales, new Paragraph(numero(totalVae, 2)?.toString(), times8bold), celdaCabeceraCentro)

        reportesPdfService.addCellTb(tablaTotales, new Paragraph("COSTOS INDIRECTO", times8bold), prmsFilaIzquierda)
        reportesPdfService.addCellTb(tablaTotales, new Paragraph(numero(totalIndi, 2)?.toString(), times8bold), prmsFilaDerecha)
        reportesPdfService.addCellTb(tablaTotales, new Paragraph("TOTAL", times8bold), prmsFila)
        reportesPdfService.addCellTb(tablaTotales, new Paragraph("TOTAL", times8bold), prmsFila)

        reportesPdfService.addCellTb(tablaTotales, new Paragraph("COSTO TOTAL DEL RUBRO", times8bold), prmsFilaIzquierda)
        reportesPdfService.addCellTb(tablaTotales, new Paragraph(numero((totalRubro + totalIndi), 2)?.toString(), times8bold), prmsFilaDerecha)
        reportesPdfService.addCellTb(tablaTotales, new Paragraph("PESO", times8bold), prmsFila)
        reportesPdfService.addCellTb(tablaTotales, new Paragraph("VAE", times8bold), prmsFila)

        reportesPdfService.addCellTb(tablaTotales, new Paragraph("PRECIO UNITARIO \$USD", times8bold), celdaCabeceraIzquierda2)
        reportesPdfService.addCellTb(tablaTotales, new Paragraph(numero((totalRubro + totalIndi), 2)?.toString(), times8bold), celdaCabeceraDerecha2)
        reportesPdfService.addCellTb(tablaTotales, new Paragraph("RELATIVO", times8bold), celdaCabeceraCentro2)
        reportesPdfService.addCellTb(tablaTotales, new Paragraph("(%)", times8bold), celdaCabeceraCentro2)

        PdfPTable tablaNota = new PdfPTable(2);
        tablaNota.setWidthPercentage(100);
        tablaNota.setWidths(arregloEnteros([6, 94]))
        if(rubro?.codigo?.split('-')[0] == 'TR'){
            reportesPdfService.addCellTb(tablaNota, new Paragraph("Distancia a la escombrera:", times8bold), prmsFilaIzquierda)
            reportesPdfService.addCellTb(tablaNota, new Paragraph("${obra?.distanciaDesalojo}" + "km", times8normal), prmsFilaIzquierda)
        }
        reportesPdfService.addCellTb(tablaNota, new Paragraph("Nota:", times8bold), prmsFilaIzquierda)
        reportesPdfService.addCellTb(tablaNota, new Paragraph("Los cálculos se hacen con todos los " +
                "decimales y el resultado final se lo redondea a dos decimales.", times8normal), prmsFilaIzquierda)

        document.add(headers)
        document.add(tablaCoeficiente)
        document.add(tablaEquipos)
        document.add(tablaManoObra)
        document.add(tablaMateriales)
        if(params.desglose != '0'){
            document.add(tablaTransporte)
        }
        document.add(tablaIndirectos)
        document.add(tablaTotales)
        document.add(tablaNota)

        document.close();
        pdfw.close()
        byte[] b = baos.toByteArray();
        response.setContentType("application/pdf")
        response.setHeader("Content-disposition", "attachment; filename=" + name)
        response.setContentLength(b.length)
        response.getOutputStream().write(b)
    }


}
