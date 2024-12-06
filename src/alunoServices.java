import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class alunoServices implements Runnable {
       
    private Socket socket;
    private AlunoResource recurso;

    public String IndentificaMetodo(String url){
        if (url.contains("POST")) {
            return "POST";
        }
        else if(url.contains("GET")){
            return "GET";
        }else if(url.contains("DELETE")){
            return "DELETE";
        }else{
            return "ERROR";
        }
    }
    
    public String AlunoPadronizarRotas(String url){
        // aqui eu estou comunicando meu socket que gostarias de padronizar as rotas 
        // que irei observar da seguinte maneira iniciando com POST, GET ou DELETE que sao justamente os metodos necesários para ações
        String regex_post = "^POST /aluno HTTP/1.1";
        String regex_get = "^GET /aluno/\\d+ HTTP/1.1";
        String regex_delete = "^DELETE /aluno/\\d+ HTTP/1.1";
    
        // Aqui eu to pegando as strings que obedecem minha imposicao e converto-as para pattern para poder usar o matcher
        Pattern pattern_post = Pattern.compile(regex_post);
        Pattern pattern_get = Pattern.compile(regex_get);
        Pattern pattern_delete = Pattern.compile(regex_delete);
    
        //Aqui eu faço a comparação do objeto com a url e vejo se esta obedecendo realmente as imposições 
        Matcher matcher_post = pattern_post.matcher(url);
        Matcher matcher_get = pattern_get.matcher(url);
        Matcher matcher_delete = pattern_delete.matcher(url);

        // Aqui eu procuro e retorno os metodos que serão utilizados
        if(matcher_post.find()){
            return "POST";
        }
        else if(matcher_get.find()){
            return "GET";
        }
        else if(matcher_delete.find()){
            return "DELETE";
        }
        else{
            return "ERROR";
        }
    }

    // nessa funcao eu faco a mesma coisa para pegar o id do aluno que sera necessario para vizualizar seus dados
    public int GetProntuarioAluno(String url){
        Pattern pattern = Pattern.compile("\\d+");
        Matcher matcher = pattern.matcher(url);

        matcher.find();
        String number =  matcher.group();//acessando os valores do matcher .find que ficam guardados dentro do objeto matcher

        return Integer.parseInt(number);
    }

    // construtor
    public alunoServices(Socket socket, AlunoResource recurso) {
        this.socket = socket;
        this.recurso = recurso;
    }

    // padronizando as respostas http em uma funcao
    public void HTML_response(String responseBody, PrintWriter out){
            out.println("Content-Type: text/html; charset=UTF-8");
            out.println("Content-Length: " + responseBody.length());
            out.println();
            out.println(responseBody);
            out.flush();
    }

    @Override
    public void run() {
        try {
    // estabelecendo conexão e mostrando para o usuario
            System.out.println(
                    "Novo cliente conectado: " + this.socket.getInetAddress().getHostAddress().toString()
                            + " at port " + this.socket.getPort());

            try (
                // padrao de leitura e escrita do socket
                PrintWriter out = new PrintWriter(this.socket.getOutputStream());
                BufferedReader in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));){

                // recebendo mensagem
                String message = in.readLine();

                // observando a escolha de rota do usuario
                String method = this.AlunoPadronizarRotas(message);
                System.out.println(message);

                if(method == "ERROR"){
                    // respondendo o erro para o usuario
                    String responseBody = "<html><body><h3>ERROR 404</h3></body></html>";
                    out.println("HTTP/1.1 404 ERROR");
                    this.HTML_response(responseBody, out);
                }else if(method == "POST") {
                    try{
                        // zona critica
                        recurso.add_random_user(this);

                    }catch(Exception e){

                    }
                    // pegando os dados do aluno criado para poderem ser exibidos posteriormente
                        int last_added = recurso.Prontuarios_usados.size() - 1;

                        String Name = recurso.alunos.get(last_added).GetNome();
                        int Idade = recurso.alunos.get(last_added).GetIdade();
                        String responseBody = "<html><body><h3>NAME: " + Name + "</h3><br><h3>IDADE: " + Idade + "</h3></body></html>";
    
                        out.println("HTTP/1.1 200");
                        this.HTML_response(responseBody, out);
                }else if (method == "GET") {
                    // pegando o aluno pelo prontuario
                    int id = this.GetProntuarioAluno(message);

                    // mostrando os dados desse aluno
                    String responseBody = this.recurso.show_aluno(id);

                    // verificando se o aluno existe ou nao atraves do corpo do returno da funcao de getprontuarioaluno
                   if(responseBody.charAt(0) != '<'){
                       out.println("HTTP/1.1 404");
                       this.HTML_response(responseBody, out);
                   }
                   else{
                    out.println("HTTP/1.1 200");
                    this.HTML_response(responseBody, out);}
                   }else if(method == "DELETE") {
                    // mesmo conceito do get
                        int id = this.GetProntuarioAluno(message);
                        try{
                            boolean result = this.recurso.delete_aluno(id, this);
                            String responseBody;
        
                            if(result){
                                responseBody = "<html><body><h3>O aluno foi deletado.</h3></body></html>" + message;
                            }
                            else{
                                responseBody = "<html><body><h3>O aluno nao existe.</h3></body></html>" + message;
                            }
                            out.println("HTTP/1.1 200 OK");
                            this.HTML_response(responseBody, out);

                    }catch(Exception e){

                    }
                }
            }
        } catch (IOException e) {

        } finally {
            this.close();
        }
    }

    private void close() {
        try {
            this.socket.close();
        } catch(IOException e) {
            
        }
    }
}