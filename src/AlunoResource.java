import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;
import java.util.Random;

public class AlunoResource {
// definindo atributos do recurso que serao utilizados ou modificados no Services
    public ArrayList<Aluno> alunos;
    public ArrayList<Integer> Prontuarios_usados;
    private alunoServices user;
    // definindo as listas
    String[] firstNames = {"Bryan", "Luiz", "Gabriel", "Sara", "Ana", "Nicolas", "Chris", "Sophia"};
    String[] lastNames = {"Smith", "Johnson", "Brown", "Santos", "Paula", "Wilson", "Clark", "Souza"};

// construtor
    public AlunoResource(){
        this.alunos = new ArrayList<>();
        this.Prontuarios_usados = new ArrayList<>();
        this.user = null;
    }

    // definindo um objeto com o aluno se existir e o status do mesmo 
    public class result {
        Aluno Aluno;
        boolean found;

        result(Aluno Aluno, boolean found){
            this.Aluno = Aluno;
            this.found = found;
        }
    }

    public result GetAlunoByID(int id){
        for(int i = 0; i < alunos.size(); i++){
            if(alunos.get(i).GetProntuario() == id){
                return new result(alunos.get(i), true);
            }
        }
        return new result(null, false);
    }
// prontuario ja foi usado verifica isso
    public boolean Id_used(int id){
        return Prontuarios_usados.contains(id);
    }
// inteiro aleatorio para o prontuario
    public int random_int(){
        return ThreadLocalRandom.current().nextInt(1, 100 + 1);
    }
// nome aleatorio para o aluno
    public String random_name(){
        String firstName = firstNames[(int)(Math.random() * firstNames.length)];
        String lastName = lastNames[(int)(Math.random() * lastNames.length)];

        return firstName + " " + lastName;
    }
// gerar um prontuario unico para o aluno
    private int generateUniqueID() {
        int new_ID = 0;

        if (Prontuarios_usados.size() > 0) {
            new_ID = Prontuarios_usados.get(Prontuarios_usados.size() - 1) + 1;
        }

        return new_ID;
    }
// zona critica thread do aluno services 
    public synchronized void add_random_user(alunoServices new_user) throws Exception{
        // organizando a ordem das requisições quando requirido em O acesso para inclusão e exclusão de alunos deve tratar a concorrência de maneira apropriada.
        while(this.user != null && this.user != new_user){
            wait();
        }

        int new_ID = generateUniqueID();
        Aluno new_aluno = new Aluno(new_ID, random_int(), random_name());
        
        alunos.add(new_aluno);
        Prontuarios_usados.add(new_ID);
        

        notifyAll();
        this.user = null;
    }

    public String show_aluno(int id){
        result Aluno = GetAlunoByID(id);

        if(Aluno.found){
            return "<html><body><h3>" + "NOME: " + Aluno.Aluno.GetNome() + " <br>IDADE: " + Aluno.Aluno.GetIdade() + "</h3></body></html>";
        } else {
            return "<html><body><h3>Aluno nao existe</h3></body></html>";
        }
    }

    public synchronized boolean delete_aluno(int id, alunoServices new_user) throws Exception{
        while(this.user != null && this.user != new_user){
            wait();
        }

        for(int i = 0; i < this.alunos.size(); i++){
            if(this.alunos.get(i).GetProntuario() == id){
                this.alunos.remove(i);
                notifyAll();
                this.user = null;
                return true;
            }
        }
        notifyAll();
        this.user = null;
        return false;
    }
}