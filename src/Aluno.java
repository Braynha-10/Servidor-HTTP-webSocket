public class Aluno {
    // atributos da classe aluno
    private int Prontuario;
    private String Nome;
    private int Idade;

    // contrutores de aluno
    public Aluno(){
        this.Prontuario = 0;
        this.Idade = 0;
        this.Nome = null;
    }

    public Aluno(int Prontuario, int Idade, String Nome){
        this.Prontuario = Prontuario;
        this.Idade = Idade;
        this.Nome = Nome;
    }

    // Método para pegar os valores de aluno
    public int GetProntuario(){
        return this.Prontuario;
    }
    public int GetIdade(){
        return this.Idade;
    }
    public String GetNome(){
        return this.Nome;
    }
}