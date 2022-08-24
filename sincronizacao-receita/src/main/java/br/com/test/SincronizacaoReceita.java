/*
Cenário de Negócio:
Todo dia útil por volta das 6 horas da manhã um colaborador da retaguarda do Sicredi recebe e organiza as informações de 
contas para enviar ao Banco Central. Todas agencias e cooperativas enviam arquivos Excel à Retaguarda. Hoje o Sicredi 
já possiu mais de 4 milhões de contas ativas.
Esse usuário da retaguarda exporta manualmente os dados em um arquivo CSV para ser enviada para a Receita Federal, 
antes as 10:00 da manhã na abertura das agências.

Requisito:
Usar o "serviço da receita" (fake) para processamento automático do arquivo.

Funcionalidade:
0. Criar uma aplicação SprintBoot standalone. Exemplo: java -jar SincronizacaoReceita <input-file>
1. Processa um arquivo CSV de entrada com o formato abaixo.
2. Envia a atualização para a Receita através do serviço (SIMULADO pela classe ReceitaService).
3. Retorna um arquivo com o resultado do envio da atualização da Receita. Mesmo formato adicionando o resultado em uma 
nova coluna.


Formato CSV:
agencia;conta;saldo;status
0101;12225-6;100,00;A
0101;12226-8;3200,50;A
3202;40011-1;-35,12;I
3202;54001-2;0,00;P
3202;00321-2;34500,00;B
...

*/
package br.com.test;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import br.com.test.service.ReceitaService;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.CSVWriter;

public class SincronizacaoReceita {

    public static void main(String[] args) throws IOException, RuntimeException, InterruptedException  {
        
        //args exemplo = "src/main/resources/teste.csv"      
        LerFile(args[0]);
               
    }
    
    public static void LerFile(String uri) throws IOException, RuntimeException, InterruptedException {
    	var fileName = uri;
        Path myPath = Paths.get(fileName);

        CSVParser parser = new CSVParserBuilder().withSeparator(';').build();

        try (var br = Files.newBufferedReader(myPath,  StandardCharsets.UTF_8);
             var reader = new CSVReaderBuilder(br).withCSVParser(parser).withSkipLines(0)
                     .build()) {

            List<String[]> rows = reader.readAll();
            List<String[]> linhas = new ArrayList<>();
            String[] cabecalho = null;
            int validacaoCabecalho = 1;
            for (String[] row : rows) {
            	if(validacaoCabecalho == 1) {
            		cabecalho = new String[]{row[0], row[1], row[2],row[3], "resultado"};
            		validacaoCabecalho = 0;
            		continue;
            	}
            	
            	ReceitaService receitaService = new ReceitaService();
            	Boolean resultProcess = receitaService.atualizarConta(row[0], row[1].replaceAll("-", ""), Double.parseDouble(row[2].replaceAll(",", ".")), row[3]);
            	
            	String[] newline = new String[] {row[0],row[1], row[2],row[3],resultProcess.toString()};
            	linhas.add(newline);
            	
            } 
            
            if(cabecalho != null) {
               criarNovoFicheiro(linhas, cabecalho);
            }
            
           
        }
    }
    
    public static void criarNovoFicheiro(List<String[]> linhas, String[] cabecalho) throws IOException {
    	Writer writer = Files.newBufferedWriter(Paths.get("src/main/resources/newFicheiro/resultProcess.csv"));
        CSVWriter csvWriter = new CSVWriter(writer, ';',
                CSVWriter.NO_QUOTE_CHARACTER,
                CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                CSVWriter.DEFAULT_LINE_END);
        
        csvWriter.writeNext(cabecalho);
        csvWriter.writeAll(linhas);

        csvWriter.flush();
        writer.close();
    }
    
   
    
}
