package com.example.webclient;

import java.util.List;

public class Data {

	private List<SuperHero> superheros;

	public Data() {
	}

	public List<SuperHero> getSuperheros() {
		return superheros;
	}

	public void setSuperheros(List<SuperHero> superheros) {
		this.superheros = superheros;
	}

	@Override
	public String toString() {
		return "Data{" +
				"superheros=" + superheros +
				'}';
	}

	public static class SuperHero {

		private String name;

		private String power;

		private String url;

		public SuperHero() {
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getPower() {
			return power;
		}

		public void setPower(String power) {
			this.power = power;
		}

		public String getUrl() {
			return url;
		}

		public void setUrl(String url) {
			this.url = url;
		}

		@Override
		public String toString() {
			return "SuperHero{" +
					"name='" + name + '\'' +
					", power='" + power + '\'' +
					", url='" + url + '\'' +
					'}';
		}
	}

}
