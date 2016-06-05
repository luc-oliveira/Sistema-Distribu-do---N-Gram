package Processamento;

public class Gram {
	private String destino;
	private long frequencia = 0;
	
	public void setDestino(String x){
		this.destino = x;
	}
	
	public String getDestino(){
		return this.destino;
	}
	
	public void incrementaFrequencia(){
		this.frequencia += 1;
	}
	
	public long getFrequencia(){
		return this.frequencia;
	}
	
	public void somaFrequencia(long valor){
		this.frequencia += valor;
	}

}
