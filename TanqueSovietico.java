package TanqueRafael;
import TanqueRafael.complementos.*;
import robocode.*;
import java.awt.Color;
import java.awt.geom.*;
import java.util.*;

/* --
Desenvolvido por: Rafael de Araujo da Silva;
Turma: Sistemas de informação 6 fase - 2024;

Atividade Pratica Supervisionada desenvolvida para aula de Inteligencia Artificial ministrada pelo professor
Jonathan Nau na instituição Unifebe. O material ulizado para desenvolvimento está disponivel no MaterialDeApoio.txt.
-- */

/*-- CLASSE DESENVOLVIDA PARA IMPLEMENTAÇÃO DO ROBÔ. CONTA TAMBÉM COM TÉCNICAS DE MOVIMENTAÇÃO E ADAPTAÇÕES DE MIRA --*/
public class TanqueSovietico extends robocode.Robot {
	Point2D.Double minhaPosicao, posicaoAnterior, proximaPosicao; // Posições do robô: pocisão atual, pocisão anterior e próxima pocisão
	HashMap<String,Inimigo> inimigos;	// Armazena informações sobre inimigos detectados, indexados pelo nome
	Inimigo alvo; // Define o inimigo atual a ser mirado
	static HashMap<String,int[][][][]> registroEstatisticas = new HashMap<String,int[][][][]>(); // Armazena dados de fator de acerto para cada inimigo
	int direcao = 1; // Direção usada para determinar o fator de acerto
	double direcaoPerpendicular = 1; // Define uma direção perpendicular para um movimento evasivo em relação ao inimigo
	int acertos; // Contador de acertos recebidos pelo robô
	
	// Método pra definir as caracteristicas do robô
	public void run() {
		setBodyColor(Color.red);
		setGunColor(Color.red);
		setRadarColor(Color.red);
		setBulletColor(Color.red);
		setScanColor(Color.red);
		
		// Inicialização das variaveis
		inimigos = new HashMap<String,Inimigo>();
		alvo = null;
		Rectangle2D campoDeBatalha = new Rectangle2D.Double(50, 50, getBattleFieldWidth() - 100, getBattleFieldHeight() - 100);
		proximaPosicao = null;
		acertos = 0;
		
		// Loop responsável por executar continuamente as ações essenciais do robô
		while(true) {
			minhaPosicao = new Point2D.Double(getX(), getY()); // Atualiza a posição atual do robô
			if(alvo == null) { // Se não há alvo, gira o radar 360 graus para encontrar um
				turnRadarRight(360);
			} else { // Se há um alvo, gira o radar para acompanhá-lo
				double anguloRadar = robocode.util.Utils.normalRelativeAngleDegrees(Math.toDegrees(calcAngle(minhaPosicao, alvo.loc)) - getRadarHeading());
				alvo = null;
				turnRadarRight(anguloRadar);

				if(alvo == null) { // Se o alvo mudou de posição, faz uma nova varredura
					turnRadarRight(anguloRadar < 0 ? -360 - anguloRadar : 360 - anguloRadar);
				}
			}

			// ESTRATÉGIA MUITOS X MUITOS : Bloco define estratégia de evasão do robo em relação quantidade de inimigos na partida 
			if(alvo != null) { // Executa movimento evasivo se houver um alvo
				if(getOthers() > 1) { // Se houver mais de um inimigo
					if(proximaPosicao == null) { // Procura um ponto de menor risco para evitar danos
						proximaPosicao = posicaoAnterior = minhaPosicao;
					}
					// Gera e avalia 100 pontos no mapa, escolhendo o de menor risco
					for(int i = 0; i < 100; i++) {
						// Cada ponto está a uma distância aleatória entre 100 e 200 unidades, em um ângulo também aleatório
						// Para cada ponto, é verificado se está dentro dos limites do campo de batalha
						// Calcula o risco do ponto. O que tiver um risco menor, é atualizado como o próximo local de movimento
						double distanciaMovimento = (Math.random() * 100) + 100; 
						Point2D.Double cordenadaPonto = calcPoint(minhaPosicao, Math.toRadians(Math.random() * 360), distanciaMovimento);
						if(campoDeBatalha.contains(cordenadaPonto) && (calcRisk(cordenadaPonto) < calcRisk(proximaPosicao))) {
							proximaPosicao = cordenadaPonto;
						}
					}
				// ESTRATÉGIA 1X1: Em 1x1, movimenta-se perpendicularmente ao inimigo
				} else { 
					double distanciaMovimento = (Math.random() * 100) + 150; // Define a distancia de movimento entre 150 e 250 unidades
					// erifica se o próximo ponto, em uma direção perpendicular ao inimigo, está dentro dos limites do campo
					if(!campoDeBatalha.contains(calcPoint(minhaPosicao, calcAngle(minhaPosicao, alvo.loc) + Math.PI / 3 * direcaoPerpendicular, distanciaMovimento)) || ((Math.random() * (acertos % 5) > 0.6))) {
						direcaoPerpendicular = -direcaoPerpendicular; // Aleatoriamente inverte a direção perpendicular para evitar previsibilidade, com maior chance de alternância quanto mais acertos o inimigo estiver acertado
					} 
					double angulo = calcAngle(minhaPosicao, alvo.loc) + (Math.PI / 2) * direcaoPerpendicular; // Ajusta o angulo perpenticular 
					while(!campoDeBatalha.contains(calcPoint(minhaPosicao, angulo, distanciaMovimento))) {
						angulo -= direcaoPerpendicular * 0.1;
					}
					// Calcula o ponto de destino a uma distância de movimento, perpendicular ao alvo
					// Caso o ângulo resultante leve o robô para fora do campo, ele ajusta automaticamente o ângulo de movimento para evitar sair dos limites do campo de batalha
					proximaPosicao = calcPoint(minhaPosicao, angulo, distanciaMovimento);
				}
				// Calcula a distância e o ângulo para o ponto desejado, atualizando proximaPosicao
				double distanciaAlvo = minhaPosicao.distance(proximaPosicao); // Calcula a distância entre a posição atual do robô e a próxima posição
				double anguloMovimento = robocode.util.Utils.normalRelativeAngleDegrees(Math.toDegrees(calcAngle(minhaPosicao, proximaPosicao)) - getHeading()); // Calcula o angulo necessário para o robô girar em direção ao próximo destino
				posicaoAnterior = minhaPosicao; // Atualiza a variável posicaoAnterior para a posição atual minhaPosicao do robô, para que no próximo ciclo, ele possa calcular o risco de mover para a nova posição.
				
				// Ajusta para o menor ângulo de giro
				if(Math.abs(anguloMovimento) > 90) {  // Ajusta o movimento para girar na direção oposta, a fim de minimizar a quantidade de giro necessario
					anguloMovimento = robocode.util.Utils.normalRelativeAngleDegrees(anguloMovimento + 180); // Ajusta o angulo de modo que o robo gire para a direção oposta, tornando mais eficiente
					distanciaAlvo = -distanciaAlvo; // Inverte distancia por conta do giro contrario que o robo realizou
				}
				turnRight(anguloMovimento); // Gira para o ângulo calculado
				ahead(distanciaAlvo); // Move para frente ou para trás conforme necessário
			}
		}
	}
	
	// Implementação da logica referente a detecção entre robos no jogo
	// Metodo e chamado automaticamente pelo Robocode sempre que o robo escaneia outro robo
	public void onScannedRobot(ScannedRobotEvent e) {
		String nome = e.getName(); // Obtém o nome do inimigo detectado
		Inimigo inimigo;
		//  verifica se o inimigo já foi registrado em inimigos
		if(inimigos.get(nome) == null) {
			inimigo = new Inimigo(nome, calcPoint(minhaPosicao, Math.toRadians(getHeading() + e.getBearing()), e.getDistance()), e.getEnergy(), e.getBearing(), e.getHeading(), new Vector<OndaDeBalas>());
		} else { // Se não, cria um novo objeto Inimigo com as informações do novo robo escaneado como nome, posição, energia, entre outras informações
			inimigo = new Inimigo(nome, calcPoint(minhaPosicao, Math.toRadians(getHeading() + e.getBearing()), e.getDistance()), e.getEnergy(), e.getBearing(), e.getHeading(), inimigos.get(nome).ondas);
		}
		inimigos.put(nome, inimigo); // Define o inimigo usando o nome
		
		// Se não houver alvo, ou se o alvo atual for o mesmo que o inimigo detectado, ou se o inimigo estiver mais proximo, ele é escolhido como o novo alvo
		// Importante pois define e atualiza qual o alvo prioritário
		if((alvo == null) || (alvo.nome.equals(inimigo.nome)) || (e.getDistance() < alvo.loc.distance(minhaPosicao))) {
			alvo = inimigo; // Atualiza o alvo para o inimigo detectado
		}
		// Cria um registro de estatísticas para usar contra o inimigo
		// Esse registro vai ajudar a calcular a melhor forma de atacar o inimigo com base em sua movimentação e comprtamento
		int[][][][] stats = registroEstatisticas.get(nome.split(" ")[0]);
		if(stats == null) { // Se não houver estatísticas registradas, cria uma nova estrutura para armazenar elas
			stats = new int[2][9][13][31];
			registroEstatisticas.put(nome.split(" ")[0], stats);
		}
		// Define o poder do tiro com base na distância ao inimigo
		double poder = getOthers() > 1 ? 3 : Math.min(3, Math.max(600 / e.getDistance(), 1)); // Se houver mais de um inimigo, o poder do tiro é 3. Caso contrário, o poder e calculado para ser entre 1 e 3, dependendo da distancia.
		double direcaoAbsoluta = Math.toRadians(getHeading() + inimigo.anguloDisparo); // Calcula a direção absoluta do inimigo com base no angulo de disparo e a posição do robo
		
		//  Verifica a direção do inimigo com base na sua velocidade e o ângulo do disparo
		if(e.getVelocity() != 0) {
			if(Math.sin(Math.toRadians(inimigo.localizacao) - direcaoAbsoluta) * e.getVelocity() < 0) {
				direcao = -1; // Se a direção do movimento for oposta à direção calculada, define a direção como -1
			} else {
				direcao = 1; // Caso contrário, define a direção como 1
			}
		}
		
		// Acessa o registro de estatisticas do inimigo e seleciona as informações com base no número de inimigos, a velocidade do inimigo e sua distancia
		int[] statusAtual = stats[getOthers() > 1 ? 0 : 1][(int) (e.getVelocity() == 0 ? 8 : Math.abs(Math.sin(Math.toRadians(inimigo.localizacao) - direcaoAbsoluta) * e.getVelocity() / 3))][(int) (e.getDistance() / 100)];
		
		// Cria uma nova onda de bala, que representa os disparos direcionados ao inimigo
		OndaDeBalas novaOnda = new OndaDeBalas(minhaPosicao, inimigo.loc, direcaoAbsoluta, poder, getTime(), direcao, statusAtual, getTime() - 1);
		inimigo.ondas.add(novaOnda); // Adiciona a nova onda ao inimigo

		// O código verifica todas as ondas de bala ativas e remove aquelas que já acertaram o inimigo
		for(int i = 0; i < inimigo.ondas.size(); i++) {
			OndaDeBalas ondaAtual = inimigo.ondas.get(i);
			if(ondaAtual.verificaAcertoDaBala(inimigo.loc, getTime())) { // Se a onda acertou o inimigo
				inimigo.ondas.remove(ondaAtual); // Remove a onda
				i--; // Ajusta o índice após remoção
			}
		}
		
    	// Se o inimigo detectado for o alvo e o robô tiver energia suficiente, o código ajusta o ângulo de disparo e dispara
		if((inimigo == alvo) && (poder < getEnergy())) {
			int melhorIndice = 15;
			// Encontra o índice de movimento mais provável do inimigo, baseado nas estatísticas
			for(int i = 0; i < 31; i++) {
				if(statusAtual[melhorIndice] < statusAtual[i]) {
					melhorIndice = i;
				}
			}
			
			// Calcula a previsão de movimento do inimigo com base no melhor índice
			double previsaoInimigo = (double)(melhorIndice - (statusAtual.length - 1) / 2) / ((statusAtual.length - 1) / 2);
			double ajusteAngular = direcao * previsaoInimigo * novaOnda.anguloMaximoDeFuga(); // Ajusta o angulo de disparo com base na previsão
			double ajusteCanhao = Math.toDegrees(robocode.util.Utils.normalRelativeAngle(direcaoAbsoluta - Math.toRadians(getGunHeading()) + ajusteAngular)); // Ajusta a posição do canhão
			turnGunRight(ajusteCanhao); // Gira o canhão para o ângulo calculado
			fire(poder); // Dispara com o poder calculado
		}
	}
	
	// Função que define oque ocorre quando o robo é acertado por uma bala
	public void onHitByBullet(HitByBulletEvent e) {
		if(getOthers() == 1) { // Se houver apenas um inimigo no campo de batalha
			acertos++; // Incrementa o contador de acertos, indicando que o robô foi atingido por uma bala
		}
	}
	
	// Função que define oque ocorre quando o inimigo morre
	public void onRobotDeath(RobotDeathEvent e) {
		inimigos.remove(e.getName()); // Remove o inimigo do mapa de inimigos
		if((alvo != null) && (alvo.nome.equals(e.getName()))) {
			alvo = null; // Se o inimigo que morreu era o alvo, remove o alvo
		}
	}
	
	// Função que define oque ocorre quando o robo vence
	public void onWin(WinEvent e) {
		turnRight(30); // Gira 30 graus para a direita
		while(true) {
			turnLeft(180); // Gira 180 graus para a esquerda
			turnRight(180); // Gira 180 graus para a direita
		}
	}
	
	// Calcula o risco de um ponto no campo de batalha, baseado na proximidade de inimigos, bem como na energia deles e nas posiçoes do robo
	public double calcRisk(Point2D ponto) {
		double risco = 0;
		Iterator<Inimigo> it = inimigos.values().iterator(); // Itera sobre todos os inimigos no campo de batalha
		while(it.hasNext()) {
			Inimigo inimigo = it.next();
			risco += (inimigo.energia + 50) / ponto.distanceSq(inimigo.loc); // Adiciona risco baseado na proximidade e energia do inimigo
		}
		risco += 0.1 / ponto.distanceSq(posicaoAnterior); // Considera o risco relacionado à posição anterior
		risco += 0.1 / ponto.distanceSq(minhaPosicao); // Considera o risco relacionado à posição atual
		return risco; // Retorna o risco calculado
	}
	
	// Calcula um ponto no espaço, dado um ponto de origem, um angulo e uma distancia
	public Point2D.Double calcPoint(Point2D origem, double angulo, double distanciaAlvo) {
		// Calculo onde angulo é convertido em radianos para validar as novas coordenadas X e Y usando seno e cosseno.
		return new Point2D.Double(origem.getX() + distanciaAlvo * Math.sin(angulo), origem.getY() + distanciaAlvo * Math.cos(angulo));
	}
	
	// calcula o ângulo entre dois pontos no plano cartesiano para movimentação segura
	public double calcAngle(Point2D cordenadaPonto, Point2D cordenadaPonto2) {
		return Math.atan2(cordenadaPonto2.getX() - cordenadaPonto.getX(), cordenadaPonto2.getY() - cordenadaPonto.getY());
	}
}
