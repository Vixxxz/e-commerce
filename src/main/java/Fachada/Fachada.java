package Fachada;

import Dao.ClienteDAO;
import Dao.ClienteEnderecoDAO;
import Dominio.Cliente;
import Dominio.ClienteEndereco;
import Dominio.EntidadeDominio;
import Strategy.*;
import Util.Resultado;

import java.util.ArrayList;
import java.util.List;

public class Fachada implements IFachada {
    public Fachada() {
    }

    public Resultado<String> salvarClienteEEndereco(Cliente cliente, List<ClienteEndereco> clienteEndereco) {
        StringBuilder sb = new StringBuilder();
        EncriptografaSenha criptografa = new EncriptografaSenha();
        processarValidacoes(cliente, getValidacoes(cliente), sb);
        cliente.setSenha(criptografa.processar(cliente, sb));
        for (ClienteEndereco enderecoRelacionado : clienteEndereco) {
            processarValidacoes(enderecoRelacionado, getValidacoes(enderecoRelacionado), sb);
        }
        if (!sb.isEmpty()) {
            return Resultado.erro(sb.toString());
        }
        try {
            ClienteDAO clienteDAO = new ClienteDAO();
            Resultado<Cliente> resultadoSalvarCliente = clienteDAO.salvarClienteEEndereco(cliente, clienteEndereco);
            if (!resultadoSalvarCliente.isSucesso()) {
                return Resultado.erro(resultadoSalvarCliente.getErro());
            }
            return Resultado.sucesso("Cliente e endereço salvos com sucesso!");
        } catch (Exception e) {
            System.err.println("Erro ao salvar cliente e endereço: " + e.getMessage());
            return Resultado.erro("Erro interno ao salvar cliente e endereço.");
        }
    }

    public String salvar(EntidadeDominio entidade) throws Exception {
//        try{
//            StringBuilder sb = new StringBuilder();
//            for(EntidadeDominio entidade : entidades) {
//                switch (entidade) {
//                    case Cliente cliente -> {
//                        EncriptografaSenha criptografa = new EncriptografaSenha();
//                        processarValidacoes(cliente, getValidacoes(cliente), sb);
//                        cliente.setSenha(criptografa.processar(cliente, sb));
//                    }
//                    case ClienteEndereco clienteEndereco -> processarValidacoes(clienteEndereco, getValidacoes(clienteEndereco), sb);
//                    case Bandeira bandeira -> processarValidacoes(bandeira, getValidacoes(bandeira), sb);
//                    case Cartao cartao -> processarValidacoes(cartao, getValidacoes(cartao), sb);
//                    }
//                    case Transacao transacao -> {
//                    }
//                    case Log log -> {
//                    }
//                    case null, default ->
//                            throw new IllegalArgumentException("Tipo de entidade não suportado: " + entidade);
//                }
//            }
//            if (sb.isEmpty()) {
//                entidades = salvaEntidades(entidades, sb);
//            } else {
//                return ("Existem erros de validação: " + sb);
//            }
//            return "CLIENTE SALVO COM SUCESSO!";
//        }catch (Exception e) {
//            throw new Exception(e.getMessage() + " " + entidades, e);
//        }
        return "AINDA NAO TA PRONTO :)";
    }

    @Override
    public Resultado<String> alterar(EntidadeDominio entidade) {
        StringBuilder sb = new StringBuilder();
        switch (entidade) {
            case Cliente cliente -> {
                EncriptografaSenha criptografa = new EncriptografaSenha();
                processarValidacoes(cliente, getValidacoes(cliente), sb);
                cliente.setSenha(criptografa.processar(cliente, sb));
                if (!sb.isEmpty()) {
                    return Resultado.erro(sb.toString());
                }
                try {
                    ClienteDAO clienteDAO = new ClienteDAO();
                    Resultado<EntidadeDominio> resultadoAlterarCliente = clienteDAO.alterar(cliente);
                    if (!resultadoAlterarCliente.isSucesso()) {
                        return Resultado.erro(resultadoAlterarCliente.getErro());
                    }
                    return Resultado.sucesso("Cliente alterado com sucesso!");
                } catch (Exception e) {
                    System.err.println("Erro ao alterar cliente: " + e.getMessage());
                    return Resultado.erro("Erro interno ao alterar cliente.");
                }
            }
            case ClienteEndereco clienteEndereco -> {
                processarValidacoes(clienteEndereco, getValidacoes(clienteEndereco), sb);
                if (!sb.isEmpty()) {
                    return Resultado.erro(sb.toString());
                }
                try {
                    ClienteEnderecoDAO clienteEnderecoDAO = new ClienteEnderecoDAO();
                    Resultado<EntidadeDominio> resultadoAlterarClienteEndereco = clienteEnderecoDAO.alterar(clienteEndereco);
                    if (!resultadoAlterarClienteEndereco.isSucesso()) {
                        return Resultado.erro(resultadoAlterarClienteEndereco.getErro());
                    }
                    return Resultado.sucesso("Endereço alterado com sucesso!");
                } catch (Exception e) {
                    System.err.println("Erro ao alterar endereço: " + e.getMessage());
                    return Resultado.erro("Erro interno ao alterar endereço.");
                }
            }
//                case Bandeira bandeira -> {
//                    processarValidacoes(bandeira, getValidacoes(bandeira), sb);
//                    if(sb.isEmpty()) {
//                        alteraBandeira(bandeira, erros);
//                    }else {
//                        throw new Exception("Existem erros de validação: " + sb);
//                    }
//                }
//                case Cartao cartao -> {
//                    processarValidacoes(cartao, getValidacoes(cartao), sb);
//                    if(sb.isEmpty()) {
//                        alteraCartao(cartao, erros);
//                    }else{
//                        throw new Exception("Existem erros de validação: " + sb);
//                    }
//                }
//                    }
//                    case Transacao transacao -> {
//                    }
//                    case Log log -> {
//                    }
            case null, default -> throw new IllegalArgumentException("Tipo de entidade não suportado: " + entidade);
        }
    }

    @Override
    public Resultado<String> excluir(EntidadeDominio entidade) {
            switch (entidade) {
                case Cliente cliente -> {
                    ClienteDAO clienteDAO = new ClienteDAO();
                    Resultado<String> resultadoCliente = clienteDAO.excluir(cliente);
                    if (!resultadoCliente.isSucesso()) {
                        return Resultado.erro(resultadoCliente.getErro());
                    }
                    return Resultado.sucesso(resultadoCliente.getValor());
                }
                case ClienteEndereco clienteEndereco ->{
                    ClienteEnderecoDAO clienteEnderecoDAO = new ClienteEnderecoDAO();
                    Resultado<String> resultadoClienteEndereco = clienteEnderecoDAO.excluir(clienteEndereco);
                    if (!resultadoClienteEndereco.isSucesso()) {
                        return Resultado.erro(resultadoClienteEndereco.getErro());
                    }
                    return Resultado.sucesso(resultadoClienteEndereco.getValor());
                }
//                case Bandeira bandeira ->{
//                    BandeiraDAO bandeiraDAO = new BandeiraDAO(connection);
//                    bandeiraDAO.excluir(bandeira);
//                }
//                case Cartao cartao ->{
//                    CartaoDAO cartaoDAO = new CartaoDAO(connection);
//                    cartaoDAO.excluir(cartao);
//                }
                case null, default -> {
                return Resultado.erro("Tipo de entidade não suportado");
                }
            }
    }

//
//    private void excluirBandeira(Bandeira bandeira) throws Exception {
//        try{
//            BandeiraDAO bandeiraDAO = new BandeiraDAO(connection);
//            bandeiraDAO.excluir(bandeira);
//        } catch (Exception e) {
//            throw new Exception(e.getMessage(), e);
//        }
//    }

    @Override
    public Resultado<List<EntidadeDominio>> consultar(EntidadeDominio entidade) {
        switch (entidade) {
            case Cliente cliente -> {
                ClienteDAO clienteDAO = new ClienteDAO();
                Resultado<List<EntidadeDominio>> resultadoEntidades = clienteDAO.consultar(entidade);
                if (!resultadoEntidades.isSucesso()) {
                    return Resultado.erro(resultadoEntidades.getErro());
                }
                return Resultado.sucesso(resultadoEntidades.getValor());
            }
            case ClienteEndereco clienteEndereco ->{
                ClienteEnderecoDAO clienteEnderecoDAO = new ClienteEnderecoDAO();
                Resultado<List<EntidadeDominio>> resultadoEntidades = clienteEnderecoDAO.consultar(entidade);
                if(!resultadoEntidades.isSucesso()) {
                    return Resultado.erro(resultadoEntidades.getErro());
                }
                return Resultado.sucesso(resultadoEntidades.getValor());
            }
//                case Bandeira bandeira ->{
//                    BandeiraDAO bandeiraDAO = new BandeiraDAO(connection);
//                    bandeiraDAO.excluir(bandeira);
//                }
//                case Cartao cartao ->{
//                    CartaoDAO cartaoDAO = new CartaoDAO(connection);
//                    cartaoDAO.excluir(cartao);
//                }
            case null, default -> {
                return Resultado.erro("Tipo de entidade não suportado");
            }
        }
    }

    private void processarValidacoes(EntidadeDominio entidade, List<IStrategy> estrategias, StringBuilder sb) {
        for (IStrategy strategy : estrategias) {
            strategy.processar(entidade, sb);
        }
    }

    private List<IStrategy> getValidacoes(EntidadeDominio entidade) {
        List<IStrategy> validacoes = new ArrayList<>();
        if (entidade instanceof Cliente) {
            validacoes.add(new ValidaDados());
            validacoes.add(new ValidaCpf());
            validacoes.add(new ValidaEmail());
            validacoes.add(new ValidaSenha());
            validacoes.add(new ValidaTelefone());
        } else if (entidade instanceof ClienteEndereco) {
            validacoes.add(new ValidaEndereco());
        } //else if (entidade instanceof Bandeira) {
//            validacoes.add(new ValidaDadosBandeira());
//        } else if (entidade instanceof Cartao) {
//            validacoes.add(new ValidaDadosCartao());
//            validacoes.add(new ValidaBandeiraExistente());
//            validacoes.add(new ValidaCartaoPreferencial());
//        }
        return validacoes;
    }
}
