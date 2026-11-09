package br.com.fiap.nexrain.api;

import java.util.List;

public class RespostaOpenAi {

    private List<Escolha> choices;

    public RespostaOpenAi() {
    }

    public List<Escolha> getChoices() {
        return choices;
    }

    public void setChoices(List<Escolha> choices) {
        this.choices = choices;
    }

    public String getConteudo() {
        if (choices == null || choices.isEmpty() || choices.get(0).getMessage() == null) {
            return null;
        }
        return choices.get(0).getMessage().getContent();
    }

    public static class Escolha {

        private Mensagem message;

        public Mensagem getMessage() {
            return message;
        }

        public void setMessage(Mensagem message) {
            this.message = message;
        }
    }

    public static class Mensagem {

        private String role;
        private String content;

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }
}
