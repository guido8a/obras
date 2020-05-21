

<html>
<head>
    <meta name="layout" content="main">
    <script src="${resource(dir: 'js/jquery/plugins/jquery-validation-1.9.0', file: 'jquery.validate.min.js')}"></script>
    <script src="${resource(dir: 'js/jquery/plugins/jquery-validation-1.9.0', file: 'messages_es.js')}"></script>

    <script src="${resource(dir: 'js/jquery/plugins/', file: 'jquery.livequery.js')}"></script>



    <style type="text/css">

    .texto {
        font-family : arial;
        font-size: 12px;
    }

    .tab {
        height     : 410px !important;
        overflow-x : hidden;
        overflow-y : hidden;
    }

    .inputVar {
        width : 65px;
    }
    .margen {
        margin-left: 40px;
        width: 500px;
    }


    </style>


    <title>Variables</title>
</head>

<body>

<g:if test="${flash.message}">
    <div class="row">
        <div class="span12">
            <div class="alert ${flash.clase ?: 'alert-info'}" role="status">
                <a class="close" data-dismiss="alert" href="#">×</a>
                ${flash.message}
            </div>
        </div>
    </div>
</g:if>

<g:form controller="parametros" action="saveFactores" name="frmSave-var">
    <g:hiddenField name="id" value="${"1"}"/>
%{--<g:hiddenField name="idO" value="${obra?.listaManoObra?.id}"/>--}%
    <div id="tabs" style="height: 450px; width: 750px">
        <ul>
            %{--<li><a href="#tab-transporte">Transporte</a></li>--}%
            <li><a href="#tab-factores">Factores</a></li>
            <li><a href="#tab-indirecto">Costos Indirectos</a></li>
            %{--<li class="desglose"><a href="#tab-desglose">Desglose de Transporte</a></li>--}%
        </ul>
        <div id="tab-factores" class="tab">


            <div class="row-fluid margen" style="margin-top: 20px;">
                <div class="span5" style="margin-left: 400px">
                    <b>Valores por defecto</b>
                </div>
            </div>

            <div class="row-fluid margen" style="margin-top: 10px;">
                <div class="span5">
                    Factor de reducción
                </div>

                <div class="span2">
                    <g:textField type="text" name="factorReduccion" class="inputVar num2" autocomplete='off' value="${g.formatNumber(number: (paux?.factorReduccion) ?: par.factorReduccion, maxFractionDigits: 0, minFractionDigits: 0, format: '##,##0', locale: 'ec')}"/>
                </div>

                <div class="span2" style="margin-left: 140px">
                    <g:textField type="text" name="factorReduccionOr" class="inputVar" autocomplete='off' value="${"10"}" readonly="true"/>
                </div>

            </div>
            <div class="row-fluid margen">
                <div class="span5">
                    Velocidad
                </div>

                <div class="span2">
                    <g:textField type="text" name="factorVelocidad" class="inputVar num2" autocomplete='off' value="${g.formatNumber(number: (paux?.factorVelocidad) ?: par.factorVelocidad, maxFractionDigits: 0, minFractionDigits: 0, format: '##,##0', locale: 'ec')}"/>
                </div>

                <div class="span2" style="margin-left: 140px">
                    <g:textField type="text" name="factorVelocidadOr" class="inputVar" autocomplete='off' value="${"40"}" readonly="true"/>
                </div>
            </div>

            <div class="row-fluid margen">
                <div class="span5">
                    Capacidad Volquete
                </div>

                <div class="span2">
                    <g:textField type="text" name="capacidadVolquete" class="inputVar num2" autocomplete='off' value="${g.formatNumber(number: (paux?.capacidadVolquete) ?: par.capacidadVolquete, maxFractionDigits: 0, minFractionDigits: 0, format: '##,##0', locale: 'ec')}"/>
                </div>

                <div class="span2" style="margin-left: 140px">
                    <g:textField type="text" name="capacidadVolqueteOr" class="inputVar" autocomplete='off' value="${"8"}" readonly="true"/>
                </div>
            </div>
            <div class="row-fluid margen">

                <div class="span5">
                    Reducción/Tiempo
                </div>

                <div class="span2">
                    <g:textField type="text" name="factorReduccionTiempo" class="inputVar num2" autocomplete='off' value="${g.formatNumber(number: (paux?.factorReduccionTiempo) ?: par.factorReduccionTiempo, maxFractionDigits: 0, minFractionDigits: 0, format: '##,##0', locale: 'ec')}"/>
                </div>

                <div class="span2" style="margin-left: 140px">
                    <g:textField type="text" name="factorReduccionTiempoOr" class="inputVar" autocomplete='off' value="${"24"}" readonly="true"/>
                </div>
            </div>

            <div class="row-fluid margen">
                <div class="span5">
                    Factor Volumen
                </div>

                <div class="span2">
                    <g:textField type="text" name="factorVolumen" class="inputVar num" value="${g.formatNumber(number: (paux?.factorVolumen) ?: par.factorVolumen, maxFractionDigits: 2, minFractionDigits: 2, format: '##,##0', locale: 'ec')}"/>
                </div>

                <div class="span2" style="margin-left: 140px">
                    <g:textField type="text" name="factorVolumenOr" class="inputVar" value="${"0.80"}" readonly="true"/>
                </div>
            </div>
            <div class="row-fluid margen">
                <div class="span5">
                    Factor Peso
                </div>

                <div class="span2">
                    <g:textField type="text" name="factorPeso" class="inputVar num" value="${g.formatNumber(number: (paux?.factorPeso) ?: par.factorPeso, maxFractionDigits: 2, minFractionDigits: 2, format: '##,##0', locale: 'ec')}"/>
                </div>

                <div class="span2" style="margin-left: 140px">
                    <g:textField type="text" name="factorPesoOr" class="inputVar " value="${"1.70"}" readonly="true"/>
                </div>
            </div>
        </div>

        <div id="tab-indirecto" class="tab">
            <div class="row-fluid" style="margin-top: 20px;">
                <div class="span4">
                    Dirección de la obra
                </div>

                <div class="span2">
                    <g:textField type="text" name="indiceCostosIndirectosObra" class="inputVar sum1 num sumAO sumTI"
                                 value="${g.formatNumber(number: (paux?.indiceCostosIndirectosObra), maxFractionDigits: 2,
                                         minFractionDigits: 2, format: '##,##0')}" tabindex="1"/>
                </div>

                <div class="span4">
                    Garantías
                </div>

                <div class="span2">
                    <g:textField type="text" name="indiceCostosIndirectosGarantias" class="inputVar sum3 num sumAO sumTI"
                                 value="${g.formatNumber(number: (paux?.indiceCostosIndirectosGarantias),
                                         maxFractionDigits: 2, minFractionDigits: 2, format: '##,##0')}" tabindex="7"/>
                </div>
            </div>

            <div class="row-fluid" style="margin-top: 10px;">
                <div class="span4">
                    Cargos Administrativos
                </div>

                <div class="span2">
                    <g:textField type="text" name="administracion" class="inputVar sum1 num sumAO sumTI"
                                 value="${g.formatNumber(number: (paux?.administracion), maxFractionDigits: 2,
                                         minFractionDigits: 2, format: '##,##0')}" tabindex="2"/>
                </div>

                <div class="span4">
                    Seguros
                </div>

                <div class="span2">
                    <g:textField type="text" name="indiceSeguros" class="inputVar sum3 num sumAO sumTI"
                                 value="${g.formatNumber(number: (paux?.indiceSeguros), maxFractionDigits: 2,
                                         minFractionDigits: 2, format: '##,##0')}" />
                </div>
            </div>

            <div class="row-fluid" style="margin-top: 10px;">
                <div class="span4">
                    Locales provisionales
                </div>

                <div class="span2">
                    <g:textField type="text" name="indiceAlquiler" class="inputVar sum1 num sumAO sumTI"
                                 value="${g.formatNumber(number: (paux?.indiceAlquiler), maxFractionDigits: 2,
                                         minFractionDigits: 2, format: '##,##0')}" tabindex="3"/>
                </div>

                <div class="span4">
                    Costos financieros
                </div>

                <div class="span2">
                    <g:textField type="text" name="indiceCostosIndirectosCostosFinancieros" class="inputVar sum3 num sumAO sumTI"
                                 value="${g.formatNumber(number: (paux?.indiceCostosIndirectosCostosFinancieros),
                                         maxFractionDigits: 2, minFractionDigits: 2, format: '##,##0')}" tabindex="8"/>
                </div>
            </div>

            <div class="row-fluid" style="margin-top: 10px;">
                <div class="span4">
                    Vehículos
                </div>

                <div class="span2">
                    <g:textField type="text" name="indiceCostosIndirectosVehiculos" class="inputVar sum1 num sumAO sumTI"
                                 value="${g.formatNumber(number: (paux?.indiceCostosIndirectosVehiculos),
                                         maxFractionDigits: 2, minFractionDigits: 2, format: '##,##0')}" tabindex="4"/>
                </div>

                <div class="span4">
                    Prevención de accidentes
                </div>

                <div class="span2">
                    <g:textField type="text" name="indiceSeguridad" class="inputVar sum3  num sumAO sumTI"
                                 value="${g.formatNumber(number: (paux?.indiceSeguridad), maxFractionDigits: 2,
                                         minFractionDigits: 2, format: '##,##0')}" tabindex="9"/>
                </div>
            </div>

            <div class="row-fluid" style="margin-top: 10px;">
                <div class="span4">
                    Servicios públicos
                </div>

                <div class="span2">
                    <g:textField type="text" name="indiceCostosIndirectosTimbresProvinciales" class="inputVar sum1 num sumAO sumTI"
                                 value="${g.formatNumber(number: (paux?.indiceCostosIndirectosTimbresProvinciales),
                                         maxFractionDigits: 2, minFractionDigits: 2, format: '##,##0')}" tabindex="5"/>
                </div>

                <div class="span4 bold" style="border-top: solid 1px #D3D3D3;">
                    Gasto Administración de Obra
                </div>

                <div class="span2">
                    <g:textField type="text" name="indiceGastoObra" class="inputVar sum2 num"
                                 value="${g.formatNumber(number: (totalObra), maxFractionDigits: 2, minFractionDigits: 2,
                                         format: '##,##0', locale: 'ec')}" tabindex="10" readonly=""/>
                </div>
            </div>

            <div class="row-fluid" style="margin-top: 10px;">
                <div class="span4">
                    Promoción
                </div>

                <div class="span2">
                    <g:textField type="text" name="indiceCostosIndirectosPromocion" class="inputVar sum1 num sumAO sumTI"
                                 value="${g.formatNumber(number: (paux?.indiceCostosIndirectosPromocion),
                                         maxFractionDigits: 2, minFractionDigits: 2, format: '##,##0')}" tabindex="6"/>
                </div>

                <div class="span4">
                    Utilidad
                </div>

                <div class="span2">
                    <g:textField type="text" name="indiceUtilidad" class="inputVar sum1 num sumTI"
                                 value="${g.formatNumber(number: (paux?.indiceUtilidad),
                                         maxFractionDigits: 2, minFractionDigits: 2, format: '##,##0')}" tabindex="6"/>
                </div>
            </div>
            <div class="row-fluid"  style="margin-top: 10px;">
                <div class="span4">
                </div>

                <div class="span2">
                </div>

                <div class="span4 bold" style="border-top: solid 1px #D3D3D3;">
                    Costo total de Indirectos
                </div>

                <div class="span2">
                    <g:textField type="text" name="totales" class="inputVar num" value="${g.formatNumber(number: (paux?.totales) ?: 0, maxFractionDigits: 2, minFractionDigits: 2, format: '##,##0', locale: 'ec')}" readonly=""/>
                </div>
            </div>
        </div>

        <div class="btn-group" style="margin-bottom: 10px; margin-top: 5px; margin-left: 300px">
            <g:link controller="inicio" action="parametros" class="btn"><i class="icon-arrow-left"></i> Regresar</g:link>
            <button class="btn btn-success" id="btnAceptar"><i class="icon-save"></i> Aceptar</button>
        </div>
    </div>
</g:form>



<script type="text/javascript">

    function validarNum(ev) {
        /*
         48-57      -> numeros
         96-105     -> teclado numerico
         188        -> , (coma)
         190        -> . (punto) teclado
         110        -> . (punto) teclado numerico
         8          -> backspace
         46         -> delete
         9          -> tab
         37         -> flecha izq
         39         -> flecha der
         */
        return ((ev.keyCode >= 48 && ev.keyCode <= 57) ||
            (ev.keyCode >= 96 && ev.keyCode <= 105) ||
            ev.keyCode == 190 || ev.keyCode == 110 ||
            ev.keyCode == 8 || ev.keyCode == 46 || ev.keyCode == 9 ||
            ev.keyCode == 37 || ev.keyCode == 39 || ev.keyCode == 188 );
    }

    $(".sum1, .sum2, .num").keydown(function (ev) {
        if (ev.keyCode == 190 || ev.keyCode == 188) {
            if ($(this).val().indexOf(".") > -1) {
                return false
            }
        }
        return validarNum(ev);
    });

    $(".num2").keydown(function (ev) {
        if (ev.keyCode == 190 || ev.keyCode == 188 || ev.keyCode == 110) {
//            if ($(this).val().indexOf(".") > -1) {
            return false
//            }
        }
        return validarNum(ev);
    });

    function suma(items, update) {
        var sum1 = 0;
        items.each(function () {
            sum1 += parseFloat($(this).val().replace(',', '.'));
        });
        update.val(number_format(sum1, 2, ",", ""));
    }

    $(function () {

        $(".sumAO").keyup(function (ev) {
            suma($(".sumAO"), $("#indiceGastoObra"));
        }).blur(function () {
            suma($(".sumAO"), $("#indiceGastoObra"));
        });

        $(".sumAO").blur();

        $(".sumTI").keyup(function (ev) {
            suma($(".sumTI"), $("#totales"));
        }).blur(function () {
            suma($(".sumTI"), $("#totales"));
        });

        $(".sumTI").blur();

        $("#tabs").tabs({
            heightStyle : "fill"
        });
    });

    $(function () {
        $(".desglose").click(function() {
            sumaDesglose();
        });
        $("#desgloseEquipo").keyup(function() {
            sumaDesglose();
        });
        $("#desgloseRepuestos").keyup(function() {
            sumaDesglose();
        });
        $("#desgloseCombustible").keyup(function() {
            sumaDesglose();
        });
        $("#desgloseMecanico").keyup(function() {
            sumaDesglose();
        });
        $("#desgloseSaldo").keyup(function() {
            sumaDesglose();
        });
    });

    function sumaDesglose() {
        var smDesglose = 0.0
        //console.log("sumadesglose")
        smDesglose = parseFloat($("#desgloseEquipo").val()) + parseFloat($("#desgloseRepuestos").val()) +
            parseFloat($("#desgloseCombustible").val()) + parseFloat($("#desgloseMecanico").val()) +
            parseFloat($("#desgloseSaldo").val())
        $("#totalDesglose").val(number_format(smDesglose, 2, ",", ""));
    }

    $("#btnAceptar").click(function () {

        $("#frmSave-var").submit();

    });
</script>

</body>
</html>