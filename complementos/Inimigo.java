package TanqueRafael.complementos;
import java.awt.geom.*;
import java.util.*;

/*--  CLASSE DESENVOLVIDA PARA REPRESENTAR UM INIMIGO NO CAMPO DE BATALHA,
      Achei melhor fazer uma classe própria para o inimigo pois permite organizar e 
	  encapsular informações especificas sobre cada adversário em tempo real durante a batalha,
	  facilitando e melhorando a analise de pontos como última posição conhecida, a velocidade, 
	  o ângulo, a energia, o tempo em que o inimigo foi detectado, entre outros atributos.
--*/

public class Inimigo {
	public String nome; // Nome do inimigo
	public Point2D.Double loc; // Localização do inimigo
	public double energia, anguloDisparo, localizacao; // Energia, ângulo de radar e direção do inimigo
	public Vector<OndaDeBalas> ondas; // Vetor lista que armazena as ondas de bala disparadas no inimigo
	
	// Construtor que inicializa os dados do inimigo
	public Inimigo(String nome, Point2D.Double loc, double energia, double anguloDisparo, double localizacao, Vector<OndaDeBalas> ondas) {
		this.nome = nome;
		this.loc = loc;
		this.energia = energia;
		this.anguloDisparo = anguloDisparo;
		this.localizacao = localizacao;
		this.ondas = ondas;
	}
}
