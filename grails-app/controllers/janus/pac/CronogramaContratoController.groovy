package janus.pac

import janus.*
import janus.ejecucion.PeriodosInec
import janus.ejecucion.ValorIndice
import org.springframework.dao.DataIntegrityViolationException

class CronogramaContratoController extends janus.seguridad.Shield {

    def preciosService
    def arreglosService
    def dbConnectionService

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def fixCrono() {
        def contrato = Contrato.get(params.id)
        def res = arreglosService.fixCronoContrato(contrato)
        render res
    }

    def index() {
        def contrato = Contrato.get(params.id)
        def cn = dbConnectionService.getConnection()
        def cn2 = dbConnectionService.getConnection()
        if (!contrato) {
            flash.message = "No se encontró el contrato"
            flash.clase = "alert-error"
            redirect(controller: 'contrato', action: "registroContrato", params: [contrato: params.id])
            return
        }
        def obraOld = contrato?.oferta?.concurso?.obra
        println "oblraOld... $obraOld"
        if (!obraOld) {
            flash.message = "No se encontró la obra"
            flash.clase = "alert-error"
            redirect(controller: 'contrato', action: "registroContrato", params: [contrato: params.id])
            return
        }


//        def existente = VolumenContrato.findByContrato(contrato)?.refresh()
//        println("ex " + existente)


        def obra = Obra.findByCodigo(obraOld.codigo+"-OF")
        if(!obra) {
            obra = obraOld
        }
        //solo copia si esta vacio el cronograma del contrato
        def cronoCntr = CronogramaContrato.countByContrato(contrato)
        def detalle = VolumenesObra.findAllByObra(obra, [sort: "orden"])

        def plazoDiasContrato = contrato.plazo
        def plazoMesesContrato = Math.ceil(plazoDiasContrato / 30);

        def plazoObra = obra.plazoEjecucionMeses + (obra.plazoEjecucionDias > 0 ? 1 : 0)

//        println plazoDiasContrato + "/30 = " + plazoMesesContrato
//        println "plazoMesesContrato: " + plazoMesesContrato + "    plazoObra: " + plazoObra

        if (cronoCntr == 0) {
            detalle.each { vol ->
//            def resto = 100
                def c = Cronograma.findAllByVolumenObra(vol)
                def resto = c.sum { it.porcentaje }
                c.eachWithIndex { crono, cont ->
                    if (cont < plazoMesesContrato) {
                        if (CronogramaContrato.countByVolumenObraAndPeriodo(crono.volumenObra, crono.periodo) == 0) {
                            def cronoContrato = new CronogramaContrato()
                            cronoContrato.properties = crono.properties
                            def pf, cf, df
//                        println "resto... " + resto
                            if (cont < c.size() - 1) {
                                pf = Math.floor(crono.porcentaje)
                                resto -= pf
                            } else {
                                pf = resto
                                resto -= pf
                            }
//                        println "resto... " + resto
                            cf = (pf * cronoContrato.cantidad) / crono.porcentaje
                            df = (pf * cronoContrato.precio) / crono.porcentaje

                            cronoContrato.porcentaje = pf
                            cronoContrato.cantidad = cf
                            cronoContrato.precio = df

                            if (!cronoContrato.save(flush: true)) {
                                println "Error al guardar el crono contrato del crono " + crono.id
                                println cronoContrato.errors
                            }

                        } else {
                            def pf = Math.floor(crono.porcentaje)
                            resto -= pf
                        }
                    }
                }
            }
            if (plazoMesesContrato > plazoObra) {
                ((plazoObra + 1)..plazoMesesContrato).each { extra ->
                    detalle.each { vol ->
                        def cronogramaCon = new CronogramaContrato([
                                contrato: contrato,
                                volumenObra: vol,
                                periodo: extra,
                                precio: 0,
                                porcentaje: 0,
                                cantidad: 0,
                        ])
                        if (!cronogramaCon.save(flush: true)) {
                            println "Error al guardar el crono contrato extra " + extra
                            println cronogramaCon.errors
                        }
                    }
                }
            }
        }

        def subpres = VolumenesObra.findAllByObra(obra, [sort: "orden"]).subPresupuesto.unique()

        def subpre = params.subpre
        if (!subpre) {
            subpre = subpres[0].id
        }

        if (subpre != "-1") {
//            detalle = VolumenesObra.findAllByObraAndSubPresupuesto(obra, SubPresupuesto.get(subpre), [sort: "orden"])
            detalle = VolumenContrato.findAllByContratoAndObraAndSubPresupuesto(contrato, obra, SubPresupuesto.get(subpre),
                    [sort: "volumenOrden"])
        } else {
//            detalle =  VolumenesObra.findAllByObra(obra, [sort: 'orden'])
            detalle =  VolumenContrato.findAllByContratoAndObra(contrato, obra, [sort: 'volumenOrden'])
        }

        def precios = [:]
        def indirecto = obra.totales / 100

        detalle.each {
            it.refresh()
            def res = preciosService.rbro_pcun_v2_item(obra.id, it.subPresupuesto.id, it.item.id)
            precios.put(it.id.toString(), res)

        }

        return [detalle: detalle, precios: precios, obra: obra, contrato: contrato, subpres: subpres, subpre: subpre]
    }




    def nuevoCronograma () {
//        println "nuevoCronograma: $params"
        def contrato = Contrato.get(params.id).refresh()
        def cn = dbConnectionService.getConnection()
        if (!contrato) {
            flash.message = "No se encontró el contrato"
            flash.clase = "alert-error"
            redirect(controller: 'contrato', action: "registroContrato", params: [contrato: params.id])
            return
        }
        def obraOld = contrato?.oferta?.concurso?.obra
        if (!obraOld) {
            flash.message = "No se encontró la obra"
            flash.clase = "alert-error"
            redirect(controller: 'contrato', action: "registroContrato", params: [contrato: params.id])
            return
        }


        def sql2 = "select * from vocr where cntr__id = ${contrato?.id}"
        def ex = cn.rows(sql2.toString())

        if(!ex || ex == ''){
            def sqlCopia = "insert into vocr(sbpr__id, cntr__id, obra__id, item__id, vocrcntd, vocrordn, vocrpcun, vocrsbtt, vocrrtcr, vocrcncp)\n" +
                    "select sbpr__id, ${contrato?.id}, ${contrato?.obra?.id}, item__id, vlobcntd, vlobordn, vlobpcun, vlobsbtt, vlobrtcr, 0 \n" +
                    "from vlob where obra__id = ${contrato?.obra?.id}"

            cn.execute(sqlCopia.toString());
            cn.close()
        }


        def obra = Obra.findByCodigo(obraOld.codigo+"-OF")
        if(!obra) {
            obra = obraOld
        }
        //solo copia si esta vacio el cronograma del contrato
        def cronoCntr = CronogramaContratado.countByContrato(contrato)
        def detalle = VolumenContrato.findAllByObra(obra, [sort: "volumenOrden"])
//        def detalleV = VolumenesObra.findAllByObra(obra, [sort: "orden"])
        def plazoDiasContrato = contrato.plazo
        def plazoMesesContrato = Math.ceil(plazoDiasContrato / 30);
        def plazoObra = obra.plazoEjecucionMeses + (obra.plazoEjecucionDias > 0 ? 1 : 0)

        println "meses: ${plazoMesesContrato}, dias: ${plazoDiasContrato},cronoCntr: $cronoCntr "
//        println "cronoCntr: $cronoCntr, detalle: ${detalle.size()}"

        if (cronoCntr == 0) {
            detalle.each { vol ->
//                def c = CronogramaContratado.findAllByVolumenContrato(vol)
//                println "buscar: ${vol.item.id}, ${vol.volumenOrden}, ${vol.obra.id}"
                def c = Cronograma.findAllByVolumenObra(VolumenesObra.findByItemAndObraAndOrdenAndObra(vol.item, vol.obra, vol.volumenOrden, vol.obra))
                def resto = c.sum { it.porcentaje }
//                println "....1 ${c.size()}"
                c.eachWithIndex { crono, cont ->
//                    println "procesa: $crono, $cont  plazo: $plazoMesesContrato"
//                    if (cont < plazoMesesContrato) {
//                        println "....2"
                        if (CronogramaContratado.countByPeriodoAndVolumenContrato(crono.periodo, vol) == 0) {
//                            println "....3"
                            def cronogramaContratado = new CronogramaContratado()
//                            cronogramaContratado.properties = crono.properties
                            cronogramaContratado.volumenContrato = vol
                            cronogramaContratado.contrato = contrato
                            cronogramaContratado.periodo = crono.periodo
                            cronogramaContratado.cantidad = crono.cantidad
                            cronogramaContratado.precio = crono.precio
                            cronogramaContratado.porcentaje = crono.porcentaje
                            cronogramaContratado.precio = crono.precio

//                            def pf, cf, df
//                        println "resto... " + resto
//                            if (cont < c.size() - 1) {
//                                pf = Math.floor(crono.porcentaje)
//                                resto -= pf
//                            } else {
//                                pf = resto
//                                resto -= pf
//                            }
//                        println "resto... " + resto
//                            cf = (pf * cronogramaContratado.cantidad) / crono.porcentaje
//                            df = (pf * cronogramaContratado.precio) / crono.porcentaje

//                            cronogramaContratado.porcentaje = pf
//                            cronogramaContratado.cantidad = cf?.toDouble()
//                            cronogramaContratado.precio = df?.toDouble()

                            if (!cronogramaContratado.save(flush: true)) {
                                println "Error al guardar el crono contrato del crono " + crono.id
                                println cronogramaContratado.errors
                            }

//                        }
//                        else {
//                            def pf = Math.floor(crono.porcentaje)
//                            resto -= pf
//                        }
                    }
                }
            }
            if (plazoMesesContrato > plazoObra) {
                ((plazoObra + 1)..plazoMesesContrato).each { extra ->
                    detalle.each { vol ->
                        def cronogramaCon = new CronogramaContratado([
                                contrato: contrato,
                                volumenContrato: vol,
                                periodo: extra,
                                precio: 0,
                                porcentaje: 0,
                                cantidad: 0,
                        ])
                        if (!cronogramaCon.save(flush: true)) {
                            println "Error al guardar el crono contrato extra " + extra
                            println cronogramaCon.errors
                        }
                    }
                }
            }
        }


        def subpres = VolumenContrato.findAllByObra(obra, [sort: "volumenOrden"]).subPresupuesto.unique()

        def subpre = params.subpre
        if (!subpre) {
            subpre = subpres[0].id
        }

        if (subpre != "-1") {
            detalle =  VolumenContrato.findAllByObraAndSubPresupuesto(obra, SubPresupuesto.get(subpre), [sort:'volumenOrden'])
        } else {
            detalle =  VolumenContrato.findAllByObra(obra, [sort: 'volumenOrden'])
        }

        def precios = [:]
//        def indirecto = obra.totales / 100

        println "detalle: $detalle"
        detalle.each {
//            it.refresh()
//            def res = preciosService.rbro_pcun_v2_item(obra.id, it.subPresupuesto.id, it.item.id)
            def res = it.volumenPrecio * it.volumenCantidad
            println "---- res: $res"
            precios.put(it.id.toString(), res)
        }

        return [detalle: detalle, precios: precios, obra: obra, contrato: contrato, subpres: subpres, subpre: subpre]
    }


    def index_bck() {

//        if (!params.id) {
//            params.id = "5"
//        }

        def contrato = Contrato.get(params.id)
        if (!contrato) {
            flash.message = "No se encontró el contrato"
            flash.clase = "alert-error"
            redirect(controller: 'contrato', action: "registroContrato", params: [contrato: params.id])
            return
        }
        def obra = contrato?.oferta?.concurso?.obra
        if (!obra) {
            flash.message = "No se encontró la obra"
            flash.clase = "alert-error"
            redirect(controller: 'contrato', action: "registroContrato", params: [contrato: params.id])
            return
        }

        //copia el cronograma de la obra a la tabla cronograma contrato (crng)
        /**
         * TODO: esto hay q cambiar cuando haya el modulo de oferente ganador:
         *  no se deberia copiar el cronograma de la obra sino del oferente ganador
         */

        //solo copia si esta vacio el cronograma del contrato
        def cronoCntr = CronogramaContrato.countByContrato(contrato)
        def detalle = VolumenesObra.findAllByObra(obra, [sort: "orden"])

        def plazoDiasContrato = contrato.plazo
        def plazoMesesContrato = Math.ceil(plazoDiasContrato / 30);

        def plazoObra = obra.plazoEjecucionMeses + (obra.plazoEjecucionDias > 0 ? 1 : 0)

//        println plazoDiasContrato + "/30 = " + plazoMesesContrato
//        println "plazoMesesContrato: " + plazoMesesContrato + "    plazoObra: " + plazoObra

        if (cronoCntr == 0) {
            detalle.each { vol ->
//            def resto = 100
                def c = Cronograma.findAllByVolumenObra(vol)
                def resto = c.sum { it.porcentaje }
                c.eachWithIndex { crono, cont ->
                    if (cont < plazoMesesContrato) {
                        if (CronogramaContrato.countByVolumenObraAndPeriodo(crono.volumenObra, crono.periodo) == 0) {
                            def cronoContrato = new CronogramaContrato()
                            cronoContrato.properties = crono.properties
                            def pf, cf, df
//                        println "resto... " + resto
                            if (cont < c.size() - 1) {
                                pf = Math.floor(crono.porcentaje)
                                resto -= pf
                            } else {
                                pf = resto
                                resto -= pf
                            }
//                        println "resto... " + resto
                            cf = (pf * cronoContrato.cantidad) / crono.porcentaje
                            df = (pf * cronoContrato.precio) / crono.porcentaje

                            cronoContrato.porcentaje = pf
                            cronoContrato.cantidad = cf
                            cronoContrato.precio = df

//                        println "arreglando los decimales:::::"
//                        println "porcentaje: " + crono.porcentaje + " --> " + cronoContrato.porcentaje
//                        println "cantidad: " + crono.cantidad + " --> " + cronoContrato.cantidad
//                        println "precio: " + crono.precio + " --> " + cronoContrato.precio

                            cronoContrato.contrato = contrato

                            if (!cronoContrato.save(flush: true)) {
                                println "Error al guardar el crono contrato del crono " + crono.id
                                println cronoContrato.errors
                            }/* else {
                    println "ok " + crono.id + "  =>  " + cronoContrato.id

                }*/
                        } else {
//                        println "no guarda, solo actualiza el porcentaje"
//                        println "resto... " + resto
                            def pf = Math.floor(crono.porcentaje)
                            resto -= pf
//                        println "resto... " + resto
                        }
                    }
                }
            }
            if (plazoMesesContrato > plazoObra) {
//                println ">>>AQUI"
                ((plazoObra + 1)..plazoMesesContrato).each { extra ->
                    detalle.each { vol ->
                        def cronoContrato = new CronogramaContrato([
                                contrato: contrato,
                                volumenObra: vol,
                                periodo: extra,
                                precio: 0,
                                porcentaje: 0,
                                cantidad: 0,
                        ])
                        if (!cronoContrato.save(flush: true)) {
                            println "Error al guardar el crono contrato extra " + extra
                            println cronoContrato.errors
                        }
                    }
                }
            }
        }

        def precios = [:]
        def indirecto = obra.totales / 100

        preciosService.ac_rbroObra(obra.id)

        detalle.each {
            def res = preciosService.precioUnitarioVolumenObraSinOrderBy("sum(parcial)+sum(parcial_t) precio ", obra.id, it.item.id)
            precios.put(it.id.toString(), (res["precio"][0] + res["precio"][0] * indirecto).toDouble().round(2))
        }

        return [detalle: detalle, precios: precios, obra: obra, contrato: contrato]

    } //index

    def saveCrono_ajax() {
//        println ">>>>>>>>>>>>>>>>>"
//        println params
        def saved = ""
        def ok = ""
        if (params.crono.class == java.lang.String) {
            params.crono = [params.crono]
        }
        def contrato = Contrato.get(params.cont.toLong())
        params.crono.each { str ->
            def parts = str.split("_")
//            println parts
            def per = parts[1].toString().toInteger()
            def vol = VolumenesObra.get(parts[0].toString().toLong())
            /*
            VolumenesObra volumenObra
            Integer periodo
            Double precio
            Double porcentaje
            Double cantidad
             */
            def cont = true
            def crono = CronogramaContrato.findAllByVolumenObraAndPeriodo(vol, per)
            if (crono.size() == 1) {
                crono = crono[0]
            } else if (crono.size() == 0) {
                crono = new CronogramaContrato()
                crono.contrato = contrato
            } else {
//                println "WTF MAS DE UN CRONOGRAMA volumen obra " + vol.id + " periodo " + per + " hay " + crono.size()
                cont = false
            }

            if (cont) {
                crono.volumenObra = vol
                crono.periodo = per
                crono.precio = parts[2].toString().toDouble()
                crono.porcentaje = parts[3].toString().toDouble()
                crono.cantidad = parts[4].toString().toDouble()
                if (crono.save(flush: true)) {
                    saved += parts[1] + ":" + crono.id + ";"
                    ok = "OK"
                } else {
                    ok = "NO"
                    println crono.errors
                }
            }
        }
        render ok + "_" + saved
    }

    def deleteRubro_ajax() {
        def ok = 0, no = 0
        def vol = VolumenesObra.get(params.id)
        CronogramaContrato.findAllByVolumenObra(vol).each { cr ->
            try {
                cr.delete(flush: true)
                ok++
            } catch (DataIntegrityViolationException e) {
                no++
            }
        }
        render "ok:" + ok + "_no:" + no
    }

    def deleteCronograma_ajax() {
        def ok = 0, no = 0
        def obra = Obra.get(params.obra)
        VolumenesObra.findAllByObra(obra, [sort: "orden"]).each { vo ->
            CronogramaContrato.findAllByVolumenObra(vo).each { cr ->
                try {
                    cr.delete(flush: true)
                    ok++
                } catch (DataIntegrityViolationException e) {
                    no++
                }
            }

        }
        render "ok:" + ok + "_no:" + no
    }

    def graficos2() {
//        println("params " + params)
        def obra = Obra.get(params.obra)
        def contrato = Contrato.get(params.contrato)
        return [params: params, contrato: contrato, obra: obra, nuevo: params.nuevo]
    }


    def saveCronoNuevo_ajax () {
//        println("params " + params)
        def saved = ""
        def ok = ""
        if (params.crono.class == java.lang.String) {
            params.crono = [params.crono]
        }
        def contrato = Contrato.get(params.cont.toLong())
        params.crono.each { str ->
            def parts = str.split("_")
            def per = parts[1].toString().toInteger()
            def vol = VolumenContrato.get(parts[0].toString().toLong())
            def cont = true
            def crono = CronogramaContratado.findAllByVolumenContratoAndPeriodo(vol, per)
            if (crono.size() == 1) {
                crono = crono[0]
            } else if (crono.size() == 0) {
                crono = new CronogramaContratado()
                crono.contrato = contrato
            } else {
                println "error" + vol.id + " periodo " + per + " hay " + crono.size()
                cont = false
            }

            if (cont) {
                crono.volumenContrato = vol
                crono.periodo = per
                crono.precio = parts[2].toString().toDouble()
                crono.porcentaje = parts[3].toString().toDouble()
                crono.cantidad = parts[4].toString().toDouble()
                if (crono.save(flush: true)) {
                    saved += parts[1] + ":" + crono.id + ";"
                    ok = "OK"
                } else {
                    ok = "NO"
                    println crono.errors
                }
            }
        }
        render ok + "_" + saved

    }

    def deleteRubroNuevo_ajax () {
//        println("params borrar " + params)
        def ok = 0, no = 0
        def vol = VolumenContrato.get(params.id)
        CronogramaContratado.findAllByVolumenContrato(vol).each { cr ->
            try {
                cr.delete(flush: true)
                ok++
            } catch (DataIntegrityViolationException e) {
                no++
            }
        }
        render "ok:" + ok + "_no:" + no
    }

    def deleteCronogramaNuevo_ajax () {
//        println("params " + params)
        def ok = 0, no = 0
        def obra = Obra.get(params.obra)
        VolumenContrato.findAllByObra(obra, [sort: "volumenOrden"]).each { vo ->
            CronogramaContratado.findAllByVolumenContrato(vo).each { cr ->
                try {
                    cr.delete(flush: true)
                    ok++
                } catch (DataIntegrityViolationException e) {
                    no++
                }
            }

        }
        render "ok:" + ok + "_no:" + no
    }

    def modificarCantidad_ajax (){
//        println("params " + params)
        def volumen = VolumenContrato.get(params.id)
        def cantidadActual = volumen.volumenCantidad
        def cantidadComp = volumen.cantidadComplementaria
        def cantidad = cantidadActual.toDouble() + cantidadComp.toDouble()
        return[volumen: volumen, cantidad: cantidad]
    }

    def guardarCantidad_ajax () {
        println("params " + params)
        def volumen = VolumenContrato.get(params.id)
        def cantidadComplementaria = params.volumenCantidad.toDouble()
        def cantidadActual = volumen.volumenCantidad + volumen.cantidadComplementaria
        def cantidadNueva = cantidadActual + cantidadComplementaria
        def nuevoTotal = cantidadNueva.toDouble() * volumen.volumenPrecio

        println("cantidad " + cantidadNueva)
        println("total " + nuevoTotal)

        volumen.cantidadComplementaria = cantidadComplementaria.toDouble()
        volumen.volumenSubtotal = nuevoTotal.toDouble()

        println("--> " +  volumen.cantidadComplementaria )
        println("--> " +  volumen.volumenSubtotal )

        try{
            volumen.save(flush: true)
            println("- " + volumen.volumenSubtotal)
            render "ok"
        }catch (DataIntegrityViolationException e){
            println("error al modificar la cantidad complementaria " + e)
            render "no"
        }

    }


    def editarVocr() {
        println "--> $params"
        def sbpr = []
        def sql = "select distinct sbpr__id from vocr where cntr__id = ${params.id}"
        def cn = dbConnectionService.getConnection()
        cn.eachRow(sql.toString()) { d ->
            sbpr.add( SubPresupuesto.get(d.sbpr__id) )
        }
        [subpresupuestos: sbpr, cntr: params.id]
    }

    def tablaValores() {
        def cn = dbConnectionService.getConnection()
        def cn1 = dbConnectionService.getConnection()
        def suma = 0
        def totl = ""
        println params

        def sqlTx = "select vocr__id id, itemnmbr, unddcdgo, vocrcntd, vocrpcun, vocrsbtt from vocr, item, undd " +
                "where item.item__id = vocr.item__id and undd.undd__id = item.undd__id and " +
                "cntr__id = ${params.cntr} "
        if(params.sbpr != '0') {
            sqlTx += "and sbpr__id = ${params.sbpr} order by vocrordn"
        } else {
            sqlTx += "order by vocrordn"
        }

        def txValor = ""
        def editar = ""
        println sqlTx

        def html = "<table class=\"table table-bordered table-striped table-hover table-condensed\" id=\"tablaPrecios\">"
        html += "<thead>"
        html += "<tr>"
        html += "<th>Indice_id</th>"
        html += "<th>Nombre del Indice</th>"
        html += "<th>Cantidad</th>"
        html += "<th>Precio</th>"
        html += "<th>Parcial</th>"

        def body = ""
        cn.eachRow(sqlTx.toString()) { d ->
            body += "<tr>"
            body += "<td>${d.id}</td>"
            body += "<td>${d.itemnmbr}</td>"

            def sbtt = ""
                editar = "editable"
                sbtt = g.formatNumber(number: d.vocrsbtt, maxFractionDigits: 2, minFractionDigits: 2, format: "##,##0", locale: "ec")
                body += "<td class='${editar} number' data-original='${d.vocrcntd}' data-cmpo='vocrcntd' " +
                        "data-id='${d.id}' data-valor='${d.vocrcntd}'>" + d.vocrcntd + '</td>'
                body += "<td class='${editar} number' data-original='${d.vocrpcun}' data-cmpo='vocrpcun' " +
                        "data-id='${d.id}' data-valor='${d.vocrpcun}'>" + d.vocrpcun + '</td>'
                body += "<td style='text-align:center' id=tt${d.id}>${sbtt}</td>"

            suma += d.vocrsbtt
        }
        html += "</tr>"
        html += "</thead>"
        html += "<tbody>"
        //println html

        cn.close()
        cn1.close()
        html += body

        totl = g.formatNumber(number: suma, maxFractionDigits: 2, minFractionDigits: 2, format: "##,##0", locale: "ec")
        html += "<tr style='font-weight: bolder' class='text-info'><td colspan='4'>Total</td><td>${totl}</td></tr>"

        html += "</tbody>"
        html += "</table>"
        //println html
        [html: html]
    }


    def actualizaVlin() {
        println "actualizaVlin: " + params
//        println("clase " + params?.item?.class)
        //formato de id:###/new _ prin _ indc _ valor
        if(params?.item?.class == java.lang.String) {
            params?.item = [params?.item]
        }

        def oks = "", nos = ""

        params.item.each {
//            println "Procesa: " + it

            def vlor = it.split("_")
            println "vlor: ${vlor}"
            def vocr = VolumenContrato.get(vlor[0].toInteger())

            if(vlor[1] == 'vocrcntd') {
                vocr.volumenCantidad = vlor[2].toDouble()
                println "cantidad: ${vocr.item.nombre} --> ${vlor[2]}"
            } else {
                vocr.volumenPrecio = vlor[2].toDouble()
                println "precio: ${vocr.item.nombre} --> ${vlor[2]}"
            }

            vocr.volumenSubtotal = vocr.volumenCantidad * vocr.volumenPrecio

            if (!vocr.save(flush: true)) {
                println "error: " + vlor
                if (nos != "") {
                    nos += ","
                }
                nos += "#" + vlor[0]
            } else {
                if (oks != "") {
                    oks += ","
                }
                oks += "#" + vlor[0]
            }
        }

        render "ok"
    }

    def corrigeCrcr() {
        def cn = dbConnectionService.getConnection()
        def suma = 0
        def totl = ""
        println params

        def sql = "update crcr set crcrprco = crcrprct * (select vocrsbtt/100 from vocr " +
                "where vocr.vocr__id = crcr.vocr__id) "
        cn.execute(sql.toString())

        sql = "update crcr set crcrcntd = crcrprct * (select vocrcntd/100 from vocr " +
                "where vocr.vocr__id = crcr.vocr__id) "
        cn.execute(sql.toString())

        sql = "select * from corrige_crcr(${params.id})"
        cn.execute(sql.toString())

        flash.message = "Cronograma corregido.."
        def url = "/contrato/registroContrato?contrato=" + params.id

        redirect( url: url)
    }


} //fin controller
