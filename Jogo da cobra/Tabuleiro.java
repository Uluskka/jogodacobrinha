import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Stack;

public class Tabuleiro extends JFrame { // Classe Tabuleiro que herda de JFrame para criar a interface do jogo

    private JPanel painel; // Painel principal onde a cobra e o obstáculo são desenhados
    private JPanel menu; // Painel de menu onde estão os botões e o placar
    private JButton iniciarButton, resetButton, pauseButton; // Botões para iniciar, reiniciar e pausar o jogo
    private JTextField placarField; // Campo de texto para exibir o placar
    private JComboBox<String> modoBordaCombo; // ComboBox para selecionar o modo de borda (colisão ou ressurgir)
    private int larguraTabuleiro = 600; // Largura do tabuleiro de jogo
    private int alturaTabuleiro = 600; // Altura do tabuleiro de jogo
    private int bordaLargura = 10; // Largura da borda do tabuleiro
    private int placar = 0; // Variável para armazenar o placar do jogo
    private String direcao = "direita"; // Direção inicial da cobra
    private long tempoAtualizacao = 100; // Tempo de atualização para o movimento da cobra
    private int incremento = 10; // Incremento de movimento da cobra em pixels
    private Quadrado obstaculo; // Objeto obstáculo
    private Stack<Quadrado> cobraPartes; // Estrutura de dados para armazenar as partes da cobra
    private boolean jogoPausado = false; // Variável para verificar se o jogo está pausado
    private boolean gameOver = false; // Variável para verificar se o jogo terminou
    private String modoBorda = "colisao"; // Variável que define o comportamento ao tocar a borda (colisão ou ressurgir)

    public Tabuleiro() { // Construtor da classe Tabuleiro
        setUndecorated(true); // Remove a decoração padrão da janela

        cobraPartes = new Stack<>(); // Inicializa a pilha de partes da cobra
        Quadrado cobraCabeca = new Quadrado(10, 10, Color.GREEN.darker()); // Cria a cabeça da cobra
        cobraCabeca.x = larguraTabuleiro / 2; // Define a posição inicial da cabeça no centro do tabuleiro
        cobraCabeca.y = alturaTabuleiro / 2;
        cobraPartes.push(cobraCabeca); // Adiciona a cabeça da cobra na pilha de partes

        obstaculo = new Quadrado(10, 10, Color.RED); // Cria o obstáculo com cor vermelha
        reposicionarObstaculo(); // Posiciona o obstáculo em uma posição aleatória

        setSize(larguraTabuleiro + 2 * bordaLargura, alturaTabuleiro + 2 * bordaLargura + 50); // Define o tamanho da janela
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Define o comportamento ao fechar a janela
        setLocationRelativeTo(null); // Centraliza a janela na tela
        setResizable(false); // Impede o redimensionamento da janela

        menu = new JPanel(); // Cria o painel de menu
        menu.setLayout(new FlowLayout()); // Define o layout do menu
        menu.setPreferredSize(new Dimension(larguraTabuleiro + 2 * bordaLargura, 50)); // Define o tamanho do menu

        ImageIcon originalLogoIcon = new ImageIcon("images/logo.jpeg"); // Ícone do logo
        Image logoImage = originalLogoIcon.getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH); // Ajusta o tamanho do logo
        ImageIcon logoIcon = new ImageIcon(logoImage);

        JLabel logoEsquerda = new JLabel(logoIcon); // Logo do lado esquerdo
        JLabel logoDireita = new JLabel(logoIcon); // Logo do lado direito

        iniciarButton = new JButton("Iniciar"); // Botão iniciar
        resetButton = new JButton("Reiniciar"); // Botão reiniciar
        pauseButton = new JButton("Pausar"); // Botão pausar
        placarField = new JTextField("Placar: 0", 10); // Campo de placar inicializado com 0
        placarField.setEditable(false); // Impede a edição do campo de placar

        modoBordaCombo = new JComboBox<>(new String[]{"Colisão", "Ressurgir"}); // ComboBox para selecionar o modo de borda
        modoBordaCombo.addActionListener(e -> modoBorda = modoBordaCombo.getSelectedItem().equals("Colisão") ? "colisao" : "ressurgir");

        // Adiciona componentes ao painel de menu
        menu.add(logoEsquerda);
        menu.add(iniciarButton);
        menu.add(resetButton);
        menu.add(pauseButton);
        menu.add(new JLabel("Modo de Borda:"));
        menu.add(modoBordaCombo);
        menu.add(placarField);
        menu.add(logoDireita);

        painel = new JPanel() { // Painel principal para o jogo
            @Override
            protected void paintComponent(Graphics g) { // Método para desenhar os componentes
                super.paintComponent(g);
                setBackground(new Color(173, 216, 230)); // Define o fundo do painel

                g.setColor(Color.BLUE); // Define a cor para as bordas
                g.fillRect(0, 0, larguraTabuleiro + 2 * bordaLargura, bordaLargura); // Desenha borda superior
                g.fillRect(0, alturaTabuleiro + bordaLargura, larguraTabuleiro + 2 * bordaLargura, bordaLargura); // Desenha borda inferior
                g.fillRect(0, 0, bordaLargura, alturaTabuleiro + 2 * bordaLargura); // Desenha borda esquerda
                g.fillRect(larguraTabuleiro + bordaLargura, 0, bordaLargura, alturaTabuleiro + 2 * bordaLargura); // Desenha borda direita

                for (int i = 0; i < cobraPartes.size(); i++) { // Laço para desenhar cada parte da cobra
                    Quadrado parte = cobraPartes.get(i);
                    g.setColor(i % 2 == 0 ? Color.BLACK : Color.GREEN.darker()); // Alterna a cor das partes
                    g.fillRect(parte.x + bordaLargura, parte.y + bordaLargura, parte.altura, parte.largura);
                }

                g.setColor(obstaculo.cor); // Define a cor do obstáculo
                g.fillRect(obstaculo.x + bordaLargura, obstaculo.y + bordaLargura, obstaculo.largura, obstaculo.altura); // Desenha o obstáculo
            }
        };
        painel.setPreferredSize(new Dimension(larguraTabuleiro + 2 * bordaLargura, alturaTabuleiro + 2 * bordaLargura));

        setLayout(new BorderLayout()); // Define o layout da janela
        add(menu, BorderLayout.NORTH); // Adiciona o menu ao norte
        add(painel, BorderLayout.CENTER); // Adiciona o painel ao centro
        setVisible(true); // Torna a janela visível

        iniciarButton.addActionListener(e -> { // Adiciona ação ao botão iniciar
            Iniciar();
            painel.requestFocusInWindow();
        });

        resetButton.addActionListener(e -> { // Adiciona ação ao botão reiniciar
            Reiniciar();
            painel.requestFocusInWindow();
        });

        pauseButton.addActionListener(e -> Pausar()); // Adiciona ação ao botão pausar

        painel.setFocusable(true); // Define o painel como focável
        painel.addKeyListener(new KeyAdapter() { // Adiciona um KeyListener para controlar a direção da cobra
            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) { // Muda a direção da cobra com base nas teclas pressionadas
                    case KeyEvent.VK_A: if (!direcao.equals("direita")) direcao = "esquerda"; break;
                    case KeyEvent.VK_D: if (!direcao.equals("esquerda")) direcao = "direita"; break;
                    case KeyEvent.VK_W: if (!direcao.equals("baixo")) direcao = "cima"; break;
                    case KeyEvent.VK_S: if (!direcao.equals("cima")) direcao = "baixo"; break;
                }
            }
        });
    }

    private void Iniciar() { // Método para iniciar o jogo
        if (jogoPausado) { // Verifica se o jogo está pausado
            Pausar();
        }

        new Thread(() -> { // Inicia uma nova thread para o movimento da cobra
            while (!jogoPausado && !gameOver) { // Interrompe o loop se o jogo estiver pausado ou terminado
                try {
                    Thread.sleep(tempoAtualizacao); // Pausa o thread para controlar a velocidade da cobra
                    moverCobra(); // Move a cobra
                    painel.repaint(); // Re-renderiza o painel
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void moverCobra() { // Método para mover a cobra
        Quadrado novaCabeca = new Quadrado(10, 10, Color.BLACK); // Cria uma nova cabeça para a cobra
        Quadrado cabecaAtual = cobraPartes.peek();
        novaCabeca.x = cabecaAtual.x;
        novaCabeca.y = cabecaAtual.y;

        switch (direcao) { // Move a cobra na direção atual
            case "esquerda": novaCabeca.x -= incremento; break;
            case "direita": novaCabeca.x += incremento; break;
            case "cima": novaCabeca.y -= incremento; break;
            case "baixo": novaCabeca.y += incremento; break;
        }

        if (checarAutoColisao(novaCabeca)) { // Verifica se a cobra colidiu com ela mesma
            gameOver(); // Finaliza o jogo se houver colisão
            return;
        }

        novaCabeca.cor = (cobraPartes.size() % 2 == 0) ? Color.GREEN.brighter() : Color.GREEN.darker(); // Alterna a cor da nova cabeça
        cobraPartes.push(novaCabeca); // Adiciona a nova cabeça à cobra

        if (checarColisaoComObstaculo()) { // Verifica se a cobra colidiu com o obstáculo
            placar++; // Incrementa o placar
            placarField.setText("Placar: " + placar); // Atualiza o campo de placar
            reposicionarObstaculo(); // Reposiciona o obstáculo
        } else {
            cobraPartes.remove(0); // Remove a última parte da cobra se não houve colisão com o obstáculo
        }

        if (novaCabeca.x < 0 || novaCabeca.x >= larguraTabuleiro || novaCabeca.y < 0 || novaCabeca.y >= alturaTabuleiro) { // Verifica se a cobra tocou a borda
            if (modoBorda.equals("colisao")) {
                gameOver(); // Finaliza o jogo se o modo de borda for "colisão"
            } else {
                // Ressurge a cobra do outro lado da borda se o modo for "ressurgir"
                if (novaCabeca.x < 0) novaCabeca.x = larguraTabuleiro - novaCabeca.largura;
                else if (novaCabeca.x >= larguraTabuleiro) novaCabeca.x = 0;
                if (novaCabeca.y < 0) novaCabeca.y = alturaTabuleiro - novaCabeca.altura;
                else if (novaCabeca.y >= alturaTabuleiro) novaCabeca.y = 0;
            }
        }
    }

    private boolean checarAutoColisao(Quadrado novaCabeca) { // Verifica se a nova cabeça da cobra colidiu com o corpo dela
        for (Quadrado parte : cobraPartes) {
            if (novaCabeca.x == parte.x && novaCabeca.y == parte.y) {
                return true;
            }
        }
        return false;
    }

    private boolean checarColisaoComObstaculo() { // Verifica se a cabeça da cobra colidiu com o obstáculo
        Quadrado cabeca = cobraPartes.peek();
        return cabeca.x == obstaculo.x && cabeca.y == obstaculo.y;
    }

    private void reposicionarObstaculo() { // Posiciona o obstáculo em uma nova posição aleatória
        obstaculo.x = (int) (Math.random() * larguraTabuleiro / incremento) * incremento;
        obstaculo.y = (int) (Math.random() * alturaTabuleiro / incremento) * incremento;
    }

    private void Pausar() { // Método para pausar ou continuar o jogo
        jogoPausado = !jogoPausado;
        pauseButton.setText(jogoPausado ? "Continuar" : "Pausar");
    }

    private void Reiniciar() { // Método para reiniciar o jogo
        dispose();
        new Tabuleiro();
    }

    private void gameOver() { // Método que define o fim do jogo e exibe uma mensagem de game over
        gameOver = true;
        JOptionPane.showMessageDialog(this, "Game Over! Placar: " + placar);
        Reiniciar();
    }

    private class Quadrado { // Classe interna que representa cada quadrado (parte da cobra ou obstáculo)
        int largura, altura, x, y; // Dimensões e posição do quadrado
        Color cor; // Cor do quadrado

        public Quadrado(int largura, int altura, Color cor) { // Construtor da classe Quadrado
            this.largura = largura;
            this.altura = altura;
            this.cor = cor;
        }
    }

    public static void main(String[] args) { // Método principal para iniciar o jogo
        new Tabuleiro();
    }
}
