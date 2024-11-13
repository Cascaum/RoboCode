package TanqueRafael.complementos;
import java.awt.geom.*;

/*-- CLASSE DESENVOLVIDA PARA A ONDA DE BALA DISPARADA EM DIREÇÃO A UM INIMIGO --*/
public class OndaDeBalas {
	private Point2D.Double origem, ultimoPontoConhecido; // Localização de origem e do último ponto conhecido do inimigo
	private double anguloDisparo, poder; // Ângulo de disparo e poder da bala
	private long tempoBala; // Tempo de disparo da bala
	private int direcao;// Direção da bala
	private int[] probabLocInimigo; // Array armazena dados de probabilidade da localização futura do inimigo
	private long ultimaLocInimigo; // Último tempo em que o inimigo foi detectado
	
	// Construtor para inicializar os dados da onda de disparo da bala
	public OndaDeBalas(Point2D.Double localizacao, Point2D.Double locInimigo, double anguloDisparo, double poder, long tempoBala, int direcao, int[] probabAcerto, long tempo) {
		this.origem = localizacao;
		this.ultimoPontoConhecido = locInimigo;
		this.anguloDisparo = anguloDisparo;
		this.poder = poder;
		this.tempoBala = tempoBala;
		this.direcao = direcao;
		this.probabLocInimigo = probabAcerto;
		ultimaLocInimigo = tempo;
	}
	
	// Método que calcula a velocidade da bala com base no seu poder - (maior poder = menor velocidade; menor poder = maior velocidade)
	public double calculaVelocidadeDaBala() {
		// 20: velocidade inicial máxima da bala. No Robocode, uma bala com poder 0 teria uma velocidade máxima de 20
		// 3: Um fator de redução de velocidade da bala com base no poder. Cada ponto de poder reduz a velocidade da bala em 3 unidades
		// O calculo abaixo equilibra a estratégia, pois qualquer bala com poder > 0 á velocidade diminui, mas o poder aumenta 
		return 20 - poder * 3; 
	}
	
	// Calcula o ângulo máximo de escape do inimigo em relação a bala disparada, baseado-se na velocidade da bala. Isso possibilita saber o quanto o inimigo pode se afastar do projétil disparado
	public double anguloMaximoDeFuga() {
		// 8: A velocidade máxima que um robô inimigo pode alcançar no Robocode.
		return Math.asin(8 / calculaVelocidadeDaBala()); // Baseado na razão entre a velocidade máxima do robô e a velocidade da bala

	}
	
	// Valida se a onda de bala atingiu o inimigo. Também calcula o fator de acerto necessário
	public boolean verificaAcertoDaBala(Point2D.Double inimigo, long tempo) {
		long dt = tempo - ultimaLocInimigo; // Tempo desde a última atualização da posição do inimigo
		// Variação na posição do inimigo ao longo do tempo (velocidade média em x e y)
		double dx = (inimigo.getX() - ultimoPontoConhecido.getX()) / dt;
		double dy = (inimigo.getY() - ultimoPontoConhecido.getY()) / dt;
		
        // Interpola os dados conhecidos para determinar se a onda poderia ter atingido o inimigo
		while(ultimaLocInimigo < tempo) {
			// Verifica se a distância da origem ao inimigo é menor ou igual ao alcance da onda
			if(origem.distance(inimigo) <= (ultimaLocInimigo - tempoBala) * calculaVelocidadeDaBala()) { 
				double anguloAlvo = Math.atan2(inimigo.getX() - origem.getX(), inimigo.getY() - origem.getY()); // Calcula a direção desejada (ângulo entre origem e posição do inimigo)
				double ajusteAngular = robocode.util.Utils.normalRelativeAngle(anguloAlvo - anguloDisparo); // Calcula o desvio do ângulo do alvo em relação ao ângulo de disparo
				double previsaoInimigo = Math.max(-1, Math.min(1, ajusteAngular / anguloMaximoDeFuga())) * direcao; // Calcula o fator de acerto, limitando-o entre -1 e 1 e ajustando pela direção
				int index = (int) Math.round((probabLocInimigo.length - 1) / 2 * (previsaoInimigo + 1)); // Determina o índice da provavel localização do inimigo onde a probabilidade de acerto será incrementada
				probabLocInimigo[index]++; // Incrementa a contagem de acerto na localização correspondente
				return true; // Retorna verdadeiro indicando que a onda atingiu o inimigo
			}
			// Incrementa o tempo e atualiza a posição do inimigo com base nas velocidades dx e dy
			ultimaLocInimigo++; 
			ultimoPontoConhecido = new Point2D.Double(ultimoPontoConhecido.getX() + dx, ultimoPontoConhecido.getY() + dy);
		}
		return false; // Retorna falso se a onda não atingiu o inimigo
	}
}