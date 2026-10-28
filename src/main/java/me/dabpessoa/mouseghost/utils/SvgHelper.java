package me.dabpessoa.mouseghost.utils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;

public final class SvgHelper {

    private SvgHelper() {
        // Do nothing. Construtor protegido para evitar instanciação de classe utilitária.
    }

    public static String extrairAtributoD(String caminhoResource) {
        try {
            // 1. Carrega o arquivo SVG a partir da pasta de recursos (resources)
            InputStream inputStream = SvgHelper.class.getResourceAsStream(caminhoResource);
            if (inputStream == null) {
                throw new IllegalArgumentException("Arquivo não encontrado: " + caminhoResource);
            }

            // 2. Inicializa o inicializador do DOM Parser do Java
            var factory = DocumentBuilderFactory.newInstance();
            var builder = factory.newDocumentBuilder();
            Document doc = builder.parse(inputStream);

            // Normaliza a estrutura XML para evitar quebras de texto inesperadas
            doc.getDocumentElement().normalize();

            // 3. Busca por todas as tags <path> dentro do SVG
            NodeList listaDePaths = doc.getElementsByTagName("path");

            // Se encontrar pelo menos um path, extrai o atributo "d" do primeiro
            if (listaDePaths.getLength() > 0) {
                Element elementoPath = (Element) listaDePaths.item(0);
                return elementoPath.getAttribute("d");
            }

        } catch (Exception e) {
            return null;
        }

        return null; // Retorna nulo se não encontrar a tag ou houver erro
    }
}