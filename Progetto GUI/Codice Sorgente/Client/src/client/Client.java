package client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import controller.MainController;
import controller.PopupController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;


/**
 * Classe che si occupa di effettuare la richiesta di predict al server.
 */
public class Client extends Thread {

	private MainController control;
	private PopupController popup;
	private boolean isOpenedPopup;
	private boolean doClose;
	
	/**
	 * Oggetto della classe Socket che stabilisce la connessione con il Server.
	 */
	private Socket socket = null;

	/**
	 * Oggetto della classe ObjectOutputStream che gestisce lo stream di oggetti in uscita
	 * verso il Server.
	 */
	private ObjectOutputStream out = null;

	/**
	 * Oggetto della classe ObjectInputStream che gestisce lo stream di oggetti in entrata
	 * dal Server.
	 */
	private ObjectInputStream in = null;

	
	public void close() {
		doClose = true;
	}


	/**
	 * Costruttore di classe che si occupa di inizializzare gli attributi della classe e di
	 * invocare il metodo talking().
	 *
	 * @param address - Stringa che contiene l'indirizzo IP del Server.
	 * @param port - Intero che indica la porta su cui è in ascolto il Server.
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public Client(String address, int port, MainController m) throws IOException, ClassNotFoundException {
		this.control = m;
		socket = new Socket(address, port);
		out = new ObjectOutputStream(socket.getOutputStream());
		in = new ObjectInputStream(socket.getInputStream());
	}


	public void run() {
		
		try {
			talking();
		} catch (InterruptedException | ClassNotFoundException | IOException  e) {
			//capire se devo gestire l'uscita dal popup o dalla mainPage
			if(isOpenedPopup) {
				popup.setErrorPopup("#EXIT");
				popup.setRound("#CONTROLLER");
			} else {
				control.setErrorPopup("#EXIT");
				control.setRound("#CONTROLLER");
			}
		} finally {
			try {
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}


	private void talking() throws IOException, ClassNotFoundException, InterruptedException {

		int decision = 0;
		String answer = "";

		while (true) {
			do {
				String tableName = "";
				
				while (control.getDecision() == 0) {
					sleep(100);
				}

				decision = control.getDecision();
				control.resetDecision();
				out.writeObject(decision);
				answer = (String) in.readObject();

				if (answer.contains("@ERROR")) {
					control.setErrorMsg((String) in.readObject());
					control.setCheck(false);
					control.setRound("#CONTROLLER");
				} else {

					tableName = control.getFile();
					out.writeObject(tableName);
					answer = (String) in.readObject();
					
					if(answer.contains("@ERROR")) {	
						control.setErrorMsg((String) in.readObject());
						control.setCheck(false);
						control.setRound("#CONTROLLER");
					}
				}
			} while (!answer.contains("@CORRECT"));

			control.setCheck(true);
			control.setRound("#CONTROLLER");
			System.out.println("--> KNN caricato correttamente");

			while(control.getRound().compareTo("#CONTROLLER") == 0) {
				sleep(100);
			}
			control.setRound("#CONTROLLER");
			popup = control.getPopupController();

			//segnala che il popup è stato aperto, serve per gestire nel metodo run()
			//il caso in cui viene sollevata una eccezione a programma in corso
			isOpenedPopup = true;

			// predict
			do {
				out.writeObject(4);
				boolean flag = true; //reading example
				do {
					answer = (String) (in.readObject());
					if (!answer.contains("@ENDEXAMPLE")) {
						// sto leggendo l'esempio
						String msg = (String) (in.readObject());
						
						popup.setMsg(msg);

						popup.setRound("#CONTROLLER");
						if (answer.equals("@READSTRING")) {
							
							//Attendo che Manager mi dia l'attributo discreto
							while(popup.getRound().compareTo("#CONTROLLER") == 0) {

								//per chiudere il thread quando il popup viene chiuso
								if(doClose) {
									return;
								}
								sleep(100);
							}
							
							out.writeObject(popup.getTmp());
						} else {
							double x = 0.0;
							do {
								//Attendo che Manager mi dia l'attributo continuo
								while(popup.getRound().compareTo("#CONTROLLER") == 0) {

									//per chiudere il thread quando il popup viene chiuso
									if(doClose) {
										return;
									}
									sleep(100);
								}
								//Provo ad assegnare il testo a x, se invece il testo è errato
								//l'eccezione mi resetterà waitClient a true e rientrerà nel ciclo
								try {
									x = Double.valueOf(popup.getTmp());
								} catch (NumberFormatException ex) {
									popup.setErrorPopup("#REPEAT");
									popup.setRound("#CONTROLLER");
								}
								
							} while (popup.getRound().compareTo("#CONTROLLER") == 0);
							out.writeObject(x);
						}

					} else {
						flag = false;
					}
				} while (flag);
				//sto leggendo k
				answer = (String) (in.readObject());
				int k = 0;
				do {
					
					popup.setMsg(answer);
					popup.setRound("#CONTROLLER");
					while(popup.getRound().compareTo("#CONTROLLER") == 0) {

						//per chiudere il thread quando il popup viene chiuso
						if(doClose) {
							return;
						}
						sleep(100);
					}
					try { 
						k = Integer.valueOf(popup.getTmp());
					} catch (NumberFormatException ex) {
						popup.setErrorPopup("#REPEAT");
					}

				} while (k < 1);
				out.writeObject(k);
	
				String s = (String)in.readObject();
				System.out.println("--> Predizione ottenuta: " + s);
				popup.showButton();
				popup.setPrediction(s);
				
				//Aspetto che il Manager scelga di ripetere o meno il predict 
				popup.setRound("#CONTROLLER");
				while (popup.getRound().compareTo("#CONTROLLER") == 0) {

					//per chiudere il thread quando il popup viene chiuso
					if(doClose) {
						return;
					}
					sleep(100);
				}
				
			} while (popup.isSameKnn());
			
		}

	}

	/** 
	 * DOVE VIENE CHIAMATO??????
	 * DOVE VIENE CHIAMATO??????
	 * DOVE VIENE CHIAMATO??????
	 * DOVE VIENE CHIAMATO??????
	 * DOVE VIENE CHIAMATO??????
	 * DOVE VIENE CHIAMATO??????
	*/
	void openPopup() throws IOException {
		Stage stage;
		Parent root;

		stage = new Stage();
		root = FXMLLoader.load(getClass().getResource("popup.fxml"));
		stage.setScene(new Scene(root));
		stage.setResizable(false);
		stage.initModality(Modality.APPLICATION_MODAL);
		stage.showAndWait();
	}

}
