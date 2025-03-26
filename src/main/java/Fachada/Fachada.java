package Fachada;

import Dao.BandeiraDAO;
import Dao.CartaoDAO;
import Dao.ClienteDAO;
import Dao.ClienteEnderecoDAO;
import Dominio.*;
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

    public Resultado<String> salvar(EntidadeDominio entidade) {
        StringBuilder sb = new StringBuilder();
        try {
            switch (entidade) {
                case ClienteEndereco clienteEndereco -> {
                    processarValidacoes(clienteEndereco, getValidacoes(clienteEndereco), sb);
                    if (!sb.isEmpty()) {
                        return Resultado.erro(sb.toString());
                    }
                    ClienteEnderecoDAO clienteEnderecoDAO = new ClienteEnderecoDAO();
                    System.out.println(clienteEndereco.toString());
                    Resultado<EntidadeDominio> resultadoSalvarClienteEndereco = clienteEnderecoDAO.salvar(clienteEndereco);
                    if (!resultadoSalvarClienteEndereco.isSucesso()) {
                        return Resultado.erro(resultadoSalvarClienteEndereco.getErro());
                    }
                    return Resultado.sucesso("Endereço salvo com sucesso!");
                }
                case Bandeira bandeira -> {
                    processarValidacoes(bandeira, getValidacoes(bandeira), sb);
                    if (!sb.isEmpty()) {
                        return Resultado.erro(sb.toString());
                    }
                    BandeiraDAO bandeiraDAO = new BandeiraDAO();
                    Resultado<EntidadeDominio> resultadoSalvarBandeira = bandeiraDAO.salvar(bandeira);
                    if (!resultadoSalvarBandeira.isSucesso()) {
                        return Resultado.erro(resultadoSalvarBandeira.getErro());
                    }
                    return Resultado.sucesso("Bandeira salva com sucesso!");
                }
                case Cartao cartao -> {
                    processarValidacoes(cartao, getValidacoes(cartao), sb);
                    if (!sb.isEmpty()) {
                        return Resultado.erro(sb.toString());
                    }
                    CartaoDAO cartaoDAO = new CartaoDAO();
                    Resultado<EntidadeDominio> resultadoSalvarCartao = cartaoDAO.salvar(cartao);
                    if (!resultadoSalvarCartao.isSucesso()) {
                        return Resultado.erro(resultadoSalvarCartao.getErro());
                    }
                    return Resultado.sucesso("Cartão salvo com sucesso!");
                }
//            case Transacao transacao -> {
//            }
//            case Log log -> {
//            }
                case null, default -> {
                    return Resultado.erro("Tipo de entidade não suportado: " + entidade);
                }
            }
        } catch (Exception e) {
            System.err.println("Erro ao salvar endereço: " + e.getMessage());
            return Resultado.erro("Erro interno ao salvar endereço." + e.getMessage());
        }
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
            case Bandeira bandeira -> {
                processarValidacoes(bandeira, getValidacoes(bandeira), sb);
                if (!sb.isEmpty()) {
                    return Resultado.erro(sb.toString());
                }
                try {
                    BandeiraDAO bandeiraDAO = new BandeiraDAO();
                    Resultado<EntidadeDominio> resultadoAlterarBandeira = bandeiraDAO.alterar(bandeira);
                    if (!resultadoAlterarBandeira.isSucesso()) {
                        return Resultado.erro(resultadoAlterarBandeira.getErro());
                    }
                    return Resultado.sucesso("Bandeira alterada com sucesso!");
                } catch (Exception e) {
                    System.err.println("Erro ao alterar bandeira: " + e.getMessage());
                    return Resultado.erro("Erro interno ao alterar bandeira.");
                }
            }
            case Cartao cartao -> {
                processarValidacoes(cartao, getValidacoes(cartao), sb);
                if (!sb.isEmpty()) {
                    return Resultado.erro(sb.toString());
                }
                try {
                    CartaoDAO cartaoDAO = new CartaoDAO();
                    Resultado<EntidadeDominio> resultadoAlterarCartao = cartaoDAO.alterar(cartao);
                    if (!resultadoAlterarCartao.isSucesso()) {
                        return Resultado.erro(resultadoAlterarCartao.getErro());
                    }
                    return Resultado.sucesso("Cartão alterado com sucesso!");
                } catch (Exception e) {
                    System.err.println("Erro ao alterar cartao: " + e.getMessage());
                    return Resultado.erro("Erro interno ao alterar cartao.");
                }
            }
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
            case ClienteEndereco clienteEndereco -> {
                ClienteEnderecoDAO clienteEnderecoDAO = new ClienteEnderecoDAO();
                Resultado<String> resultadoClienteEndereco = clienteEnderecoDAO.excluir(clienteEndereco);
                if (!resultadoClienteEndereco.isSucesso()) {
                    return Resultado.erro(resultadoClienteEndereco.getErro());
                }
                return Resultado.sucesso(resultadoClienteEndereco.getValor());
            }
            case Bandeira bandeira -> {
                BandeiraDAO bandeiraDAO = new BandeiraDAO();
                Resultado<String> resultadoBandeira = bandeiraDAO.excluir(bandeira);
                if (!resultadoBandeira.isSucesso()) {
                    return Resultado.erro(resultadoBandeira.getErro());
                }
                return Resultado.sucesso(resultadoBandeira.getValor());
            }
            case Cartao cartao -> {
                CartaoDAO cartaoDAO = new CartaoDAO();
                Resultado<String> resultadoCartao = cartaoDAO.excluir(cartao);
                if (!resultadoCartao.isSucesso()) {
                    return Resultado.erro(resultadoCartao.getErro());
                }
                return Resultado.sucesso(resultadoCartao.getValor());
            }
            case null, default -> {
                return Resultado.erro("Tipo de entidade não suportado");
            }
        }
    }

    @Override
    public Resultado<List<EntidadeDominio>> consultar(EntidadeDominio entidade) {
        switch (entidade) {
            case Cliente cliente -> {
                ClienteDAO clienteDAO = new ClienteDAO();
                Resultado<List<EntidadeDominio>> resultadoEntidades = clienteDAO.consultar(cliente);
                if (!resultadoEntidades.isSucesso()) {
                    return Resultado.erro(resultadoEntidades.getErro());
                }
                return Resultado.sucesso(resultadoEntidades.getValor());
            }
            case ClienteEndereco clienteEndereco -> {
                ClienteEnderecoDAO clienteEnderecoDAO = new ClienteEnderecoDAO();
                Resultado<List<EntidadeDominio>> resultadoEntidades = clienteEnderecoDAO.consultar(clienteEndereco);
                if (!resultadoEntidades.isSucesso()) {
                    return Resultado.erro(resultadoEntidades.getErro());
                }
                return Resultado.sucesso(resultadoEntidades.getValor());
            }
            case Bandeira bandeira -> {
                BandeiraDAO bandeiraDAO = new BandeiraDAO();
                Resultado<List<EntidadeDominio>> resultadoBandeira = bandeiraDAO.consultar(bandeira);
                if (!resultadoBandeira.isSucesso()) {
                    return Resultado.erro(resultadoBandeira.getErro());
                }
                return Resultado.sucesso(resultadoBandeira.getValor());
            }
            case Cartao cartao -> {
                CartaoDAO cartaoDAO = new CartaoDAO();
                Resultado<List<EntidadeDominio>> resultadoCartao = cartaoDAO.consultar(cartao);
                if(!resultadoCartao.isSucesso()){
                    return Resultado.erro(resultadoCartao.getErro());
                }
                return Resultado.sucesso(resultadoCartao.getValor());
            }
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
        } else if (entidade instanceof Bandeira) {
            validacoes.add(new ValidaDadosBandeira());
        } else if (entidade instanceof Cartao) {
            validacoes.add(new ValidaDadosCartao());
            validacoes.add(new ValidaBandeiraExistente());
            validacoes.add(new ValidaCartaoPreferencial());
        }
        return validacoes;
    }
}
