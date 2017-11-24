package server.model;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class HangmanModel {
	private String[] dictionary;
	private String slectedWord;
	private String guessedWord;
	private int remainingAttempts;
	private int score;
	private String userProgress;
	private String message;
	private boolean success;
	private String errorMessage;
	private boolean playing;
	
	//Setter and getter functions
	public void setDictionary() {
		List<String> word_list = Arrays.asList("cat", //Set ten default words
											   "monkey", 
											   "dog", 
											   "elephant", 
											   "boy", 
											   "girl", 
											   "bedroom", 
											   "bus", 
											   "train", 
											   "sheep"); 
		try {
			word_list = Files.readAllLines(Paths.get("data/words.txt"));//Read a file
			this.dictionary = word_list.toArray(new String[word_list.size()]);
		}catch(Exception ex) {this.setErrorMessage(ex.getMessage());}
	}
	public String[] getDictionary() {
		return this.dictionary;
	}
	
	public void setSlectedWord(String slectedWord) {
		this.slectedWord = slectedWord;
	}
	public String getSelectedWord() {
		return this.slectedWord;
	}
	
	public void setGuessedWord(String guessedWord) {
		this.guessedWord = guessedWord;
	}
	public String getGuessedWord() {
		return this.guessedWord;
	}
	
	public void setRemainingAttempts(int remainingAttempts) {
		this.remainingAttempts = remainingAttempts;
	}
	public int getRemainingAttempts() {
		return this.remainingAttempts;
	}
	
	public void setScore(int score) {
		this.score = score;
	}
	public int getScore() {
		return this.score;
	}
	
	public void setUserProgress(String userProgress) {
		this.userProgress = userProgress;
	}
	public String getUserProgress() {
		return this.userProgress;
	}
	
	public void setMessage(String message) {
		this.message = message;
	}
	public String getMessage() {
		return this.message;
	}
	
	public void setSuccess(boolean success) {
		this.success = success;
	}
	public boolean getSuccess() {
		return this.success;
	}
	
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}
	public String getErrorMessage() {
		return this.errorMessage;
	}
	
	public void setPlaying(boolean playing) {
		this.playing = playing;
	}
	public boolean getPlaying() {
		return this.playing;
	}
}