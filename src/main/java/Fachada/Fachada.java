package Fachada;

import Dao.*;
import Dominio.*;
import Strategy.*;
import Util.Resultado;
import Enums.Operacao;

import java.util.ArrayList;
import java.util.List;

public class Fachada implements IFachada {
    public Fachada() {
    }


    public Resultado<String> geraDevolucao(Devolucao devolucao, List<DevolucaoProduto> devolucaoProdutos){
        StringBuilder sb = new StringBuilder();
        processarValidacoes(devolucao, getValidacoes(devolucao, Operacao.SALVAR), sb);
        for(DevolucaoProduto dp : devolucaoProdutos){
            processarValidacoes(dp, getValidacoes(dp, Operacao.SALVAR), sb);
        }
        if(!sb.isEmpty()){
            return Resultado.erro(sb.toString());
        }
        try{
            DevolucaoDAO devolucaoDAO = new DevolucaoDAO();
            Resultado<Devolucao> resultadoSalvaDevolucaoCupom = devolucaoDAO.salvaDevolucaoCupom(devolucao, devolucaoProdutos);
            if(!resultadoSalvaDevolucaoCupom.isSucesso()){
                return Resultado.erro(resultadoSalvaDevolucaoCupom.getErro());
            }
            return Resultado.sucesso("Devolução e Cupons gerados com sucesso!");
        }catch (Exception e) {
            System.err.println("Erro ao atualizar pedido, salvar devolucao e cupom: " + e.getMessage());
            return Resultado.erro("Erro interno ao atualizar pedido, salvar devolucao e cupom.");
        }
    }

    public Resultado<String> salvarTroca(TrocaSolicitada trocaSolicitada, List<TrocaSolicitadaTenis> trocaSolicitadaTenis){
        StringBuilder sb = new StringBuilder();
        processarValidacoes(trocaSolicitada, getValidacoes(trocaSolicitada, Operacao.SALVAR), sb);
        for(TrocaSolicitadaTenis tst : trocaSolicitadaTenis){
            processarValidacoes(tst, getValidacoes(tst, Operacao.SALVAR), sb);
        }
        if(!sb.isEmpty()){
            return Resultado.erro(sb.toString());
        }
        try{
            TrocaSolicitadaDAO trocaSolicitadaDAO = new TrocaSolicitadaDAO();
            Resultado<TrocaSolicitada> resultadoSalvaTrocaSolicitada = trocaSolicitadaDAO.salvaTrocaSolicitada(trocaSolicitada, trocaSolicitadaTenis);
            if(!resultadoSalvaTrocaSolicitada.isSucesso()){
                return Resultado.erro(resultadoSalvaTrocaSolicitada.getErro());
            }
            return Resultado.sucesso("Troca solicitada com sucesso!");
        }catch (Exception e) {
            System.err.println("Erro ao solicitar troca e seus produtos: " + e.getMessage());
            return Resultado.erro("Erro interno ao solicitar troca e seus produtos.");
        }
    };

    public Resultado<String> salvarPedidoProduto(Pedido pedido, List<PedidoProduto> pedidoProdutos, List<CartaoPedido> cartaoPedidos, ReservaEstoque reserva, List<Cupom>cupons){
        StringBuilder sb = new StringBuilder();
        processarValidacoes(pedido, getValidacoes(pedido, Operacao.SALVAR), sb);
        for(PedidoProduto pedidoProduto : pedidoProdutos){
            processarValidacoes(pedidoProduto, getValidacoes(pedidoProduto, Operacao.SALVAR), sb);
            reserva.setProduto(pedidoProduto.getProduto());
            VerificaReservaAtiva verificaReservaAtiva = new VerificaReservaAtiva();
            verificaReservaAtiva.processar(reserva, sb);
        }
        for(CartaoPedido cartaoPedido : cartaoPedidos){
            processarValidacoes(cartaoPedido, getValidacoes(cartaoPedido, Operacao.SALVAR), sb);
        }
        if(!cupons.isEmpty()){
            for(Cupom cupom : cupons){
                processarValidacoes(cupom, getValidacoes(cupom, Operacao.ALTERAR), sb);
            }
        }
        if(!sb.isEmpty()){
            return Resultado.erro(sb.toString());
        }
        try{
            PedidoDAO pedidoDAO = new PedidoDAO();
            Resultado<Pedido> resultadoSalvarPedidoEProduto = pedidoDAO.salvarPedidoEProduto(pedido, pedidoProdutos, cartaoPedidos, cupons);
            if (!resultadoSalvarPedidoEProduto.isSucesso()) {
                return Resultado.erro(resultadoSalvarPedidoEProduto.getErro());
            }
            return Resultado.sucesso("Pedido e produtos salvos com sucesso!");
        }catch (Exception e) {
            System.err.println("Erro ao salvar pedido e pedido_produto: " + e.getMessage());
            return Resultado.erro("Erro interno ao salvar o pedido e seus produtos.");
        }
    }

    public Resultado<String> salvarClienteEEndereco(Cliente cliente, List<ClienteEndereco> clienteEndereco) {
        StringBuilder sb = new StringBuilder();
        EncriptografaSenha criptografa = new EncriptografaSenha();
        processarValidacoes(cliente, getValidacoes(cliente, Operacao.SALVAR), sb);
        Resultado<String> resultadoEncripta = criptografa.processar(cliente, sb);
        for (ClienteEndereco enderecoRelacionado : clienteEndereco) {
            processarValidacoes(enderecoRelacionado, getValidacoes(enderecoRelacionado, Operacao.SALVAR), sb);
        }
        if (!sb.isEmpty()) {
            return Resultado.erro(sb.toString());
        }
        cliente.setSenha(resultadoEncripta.getValor());
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
                    processarValidacoes(clienteEndereco, getValidacoes(clienteEndereco, Operacao.SALVAR), sb);
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
                    processarValidacoes(bandeira, getValidacoes(bandeira, Operacao.SALVAR), sb);
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
                    processarValidacoes(cartao, getValidacoes(cartao, Operacao.SALVAR), sb);
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
                case ReservaEstoque reservaEstoque -> {
                    processarValidacoes(reservaEstoque, getValidacoes(reservaEstoque, Operacao.SALVAR), sb);
                    if(!sb.isEmpty()){
                        return Resultado.erro(sb.toString());
                    }
                    ReservaDAO reservaDAO = new ReservaDAO();
                    Resultado<String> resultadoSalvaReserva = reservaDAO.CriaOuAtualizaReserva(reservaEstoque);
                    if(!resultadoSalvaReserva.isSucesso()){
                        return Resultado.erro(resultadoSalvaReserva.getErro());
                    }
                    return Resultado.sucesso(resultadoSalvaReserva.getValor());
                }
                case null, default -> {
                    return Resultado.erro("Tipo de entidade não suportado: " + entidade);
                }
            }
        } catch (Exception e) {
            System.err.println("Erro ao salvar: " + e.getMessage());
            return Resultado.erro("Erro interno ao salvar." + e.getMessage());
        }
    }

    @Override
    public Resultado<String> alterar(EntidadeDominio entidade) {
        StringBuilder sb = new StringBuilder();
        switch (entidade) {
            case Cliente cliente -> {
                EncriptografaSenha criptografa = new EncriptografaSenha();
                processarValidacoes(cliente, getValidacoes(cliente, Operacao.ALTERAR), sb);
                Resultado<String> resultadoEncripta = criptografa.processar(cliente, sb);
                if (!sb.isEmpty()) {
                    return Resultado.erro(sb.toString());
                }
                cliente.setSenha(resultadoEncripta.getValor());
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
                processarValidacoes(clienteEndereco, getValidacoes(clienteEndereco, Operacao.ALTERAR), sb);
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
                processarValidacoes(bandeira, getValidacoes(bandeira, Operacao.ALTERAR), sb);
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
                processarValidacoes(cartao, getValidacoes(cartao, Operacao.ALTERAR), sb);
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
            case Produto produto -> {
                processarValidacoes(produto, getValidacoes(produto, Operacao.ALTERAR), sb);
                if (!sb.isEmpty()) {
                    return Resultado.erro(sb.toString());
                }
                try {
                    ProdutoDAO produtoDAO = new ProdutoDAO();
                    Resultado<EntidadeDominio> resultadoAlteraProduto = produtoDAO.alterar(produto);
                    if (!resultadoAlteraProduto.isSucesso()) {
                        return Resultado.erro(resultadoAlteraProduto.getErro());
                    }
                    return Resultado.sucesso("Produto " + produto.getSku() + " alterado com sucesso");
                } catch (Exception e) {
                    System.err.println("Erro ao alterar produto: " + e.getMessage());
                    return Resultado.erro("Erro interno ao alterar produto.");
                }
            }
            case ReservaEstoque reserva ->{
                processarValidacoes(reserva, getValidacoes(reserva, Operacao.ALTERAR), sb);
                if(!sb.isEmpty()){
                    return Resultado.erro(sb.toString());
                }
                try{
                    ReservaDAO reservaDAO = new ReservaDAO();
                    Resultado<EntidadeDominio> resultadoAlterarReserva = reservaDAO.concluiReserva(reserva);
                    if(!resultadoAlterarReserva.isSucesso()){
                        return Resultado.erro(resultadoAlterarReserva.getErro());
                    }
                    return Resultado.sucesso("Reserva alteradada com sucesso!");
                } catch (Exception e) {
                    System.err.println("Erro ao alterar reserva: " + e.getMessage());
                    return Resultado.erro("Erro interno ao alterar reserva.");
                }
            }
            case Pedido pedido ->{
                try{
                    PedidoDAO pedidoDAO = new PedidoDAO();
                    processarValidacoes(pedido, getValidacoes(pedido, Operacao.ALTERAR), sb);
                    Resultado<EntidadeDominio> resultadoAlterarPedido = pedidoDAO.alterar(pedido);
                    if(!resultadoAlterarPedido.isSucesso()){
                        return Resultado.erro(resultadoAlterarPedido.getErro());
                    }
                    return Resultado.sucesso("Pedido alterado com sucesso");
                } catch (Exception e) {
                    System.err.println("Erro ao alterar reserva: " + e.getMessage());
                    return Resultado.erro("Erro interno ao alterar reserva.");
                }
            }
            case null, default -> throw new IllegalArgumentException("Tipo de entidade não suportado: " + entidade);
        }
    }

    @Override
    public Resultado<String> excluir(EntidadeDominio entidade) {
        StringBuilder sb = new StringBuilder();
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
            case Produto produto -> {
                ProdutoDAO produtoDAO = new ProdutoDAO();
                processarValidacoes(produto, getValidacoes(produto, Operacao.EXCLUIR), sb);
                if(!sb.isEmpty()){
                    return Resultado.erro(sb.toString());
                }
                Resultado<String> resultadoProduto = produtoDAO.excluir(produto);
                if (!resultadoProduto.isSucesso()) {
                    return Resultado.erro(resultadoProduto.getErro());
                }
                return Resultado.sucesso(resultadoProduto.getValor());
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
                if (!resultadoCartao.isSucesso()) {
                    return Resultado.erro(resultadoCartao.getErro());
                }
                return Resultado.sucesso(resultadoCartao.getValor());
            }
            case Produto produto -> {
                ProdutoDAO produtoDAO = new ProdutoDAO();
                Resultado<List<EntidadeDominio>> resultadoProduto = produtoDAO.consultar(produto);
                if (!resultadoProduto.isSucesso()) {
                    return Resultado.erro(resultadoProduto.getErro());
                }
                return Resultado.sucesso(resultadoProduto.getValor());
            }
            case Transportadora transportadora -> {
                TransportadoraDAO transportadoraDAO = new TransportadoraDAO();
                Resultado<List<EntidadeDominio>> resultadoTransportadora = transportadoraDAO.consultar(transportadora);
                if(!resultadoTransportadora.isSucesso()){
                    return Resultado.erro(resultadoTransportadora.getErro());
                }
                return Resultado.sucesso(resultadoTransportadora.getValor());
            }
            case Cupom cupom -> {
                CupomDAO cupomDAO = new CupomDAO();
                Resultado<List<EntidadeDominio>> resultadoCupom = cupomDAO.consultar(cupom);
                if(!resultadoCupom.isSucesso()){
                    return Resultado.erro(resultadoCupom.getErro());
                }
                return Resultado.sucesso(resultadoCupom.getValor());
            }
            case Estoque estoque ->{
                EstoqueDAO estoqueDAO = new EstoqueDAO();
                Resultado<List<EntidadeDominio>> resultadoEstoque = estoqueDAO.consultar(estoque);
                if(!resultadoEstoque.isSucesso()){
                    return Resultado.erro(resultadoEstoque.getErro());
                }
                return Resultado.sucesso(resultadoEstoque.getValor());
            }
            case ReservaEstoque reserva ->{
                ReservaDAO reservaDAO = new ReservaDAO();
                Resultado<List<EntidadeDominio>> resultadoReserva = reservaDAO.consultar(reserva);
                if(!resultadoReserva.isSucesso()){
                    return Resultado.erro(resultadoReserva.getErro());
                }
                return Resultado.sucesso(resultadoReserva.getValor());
            }
            case Pedido pedido ->{
                PedidoDAO pedidoDAO = new PedidoDAO();
                Resultado<List<EntidadeDominio>> resultadoPedido = pedidoDAO.consultar(pedido);
                if(!resultadoPedido.isSucesso()){
                    return Resultado.erro(resultadoPedido.getErro());
                }
                return Resultado.sucesso(resultadoPedido.getValor());
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

    private List<IStrategy> getValidacoes(EntidadeDominio entidade, Operacao operacao) {
        List<IStrategy> validacoes = new ArrayList<>();
        switch (entidade) {
            case Cliente ignored -> {
                validacoes.add(new ValidaDados());
                validacoes.add(new ValidaCpf());
                validacoes.add(new ValidaEmail());
                validacoes.add(new ValidaSenha());
                validacoes.add(new ValidaTelefone());
            }
            case ClienteEndereco ignored -> validacoes.add(new ValidaEndereco());
            case Cartao ignored -> {
                validacoes.add(new ValidaDadosCartao());
                validacoes.add(new ValidaBandeiraExistente());
                validacoes.add(new ValidaCartaoPreferencial());
            }
            case Bandeira ignored -> validacoes.add(new ValidaDadosBandeira());
            case Produto ignored -> {
                switch (operacao) {
                    case SALVAR -> {
                        validacoes.add(new VerificaDadosProduto());
                        validacoes.add(new VerificaDuplicataProduto());
                        validacoes.add(new VerificaAlteracaoMarca());
                        validacoes.add(new VerificaAlteracaoCategoria());
                    }
                    case EXCLUIR -> validacoes.add(new VerificaExistenciaProduto());
                    case ALTERAR -> {
                        validacoes.add(new VerificaDadosProduto());
                        validacoes.add(new VerificaExistenciaProduto());
                        validacoes.add(new VerificaAlteracaoMarca());
                        validacoes.add(new VerificaAlteracaoCategoria());
                    }
                }
            }
            case Pedido ignore ->{
                switch (operacao){
                    case SALVAR -> {
                        validacoes.add(new ValidaDadosPedido());
                    }
                }
            }
            case PedidoProduto ignore ->{
                switch (operacao){
                    case SALVAR -> {
                        validacoes.add(new ValidaDadosPedidoProduto());
                    }
                }
            }
            case CartaoPedido ignore ->{
                switch (operacao){
                    case SALVAR -> {
                        validacoes.add(new ValidaDadosCartaoPedido());
                    }
                }
            }
            case ReservaEstoque ignore ->{
                switch (operacao){
                    case SALVAR -> {
                        validacoes.add(new ValidaDadosReserva());
                        validacoes.add(new ValidaEstoque());
                    }
                }
            }
            case Devolucao ignore ->{
                switch (operacao){
                    case SALVAR -> {
                        validacoes.add(new ValidaDadosDevolucao());
                        validacoes.add(new VerificaStatusPedidoParaDevolucao());
                    }
                }
            }
            case Cupom ignore ->{
                switch (operacao){
                    case SALVAR -> {
                        validacoes.add(new ValidaDadosCupom());
                    }
                }
            }
            case DevolucaoProduto ignore ->{
                switch (operacao){
                    case SALVAR ->{
                        validacoes.add(new ValidaDadosDevolucaoProduto());
                    }
                }
            }
            case TrocaSolicitada ignore ->{
                switch (operacao){
                    case SALVAR -> {
                        validacoes.add(new ValidaDadosTrocaSolicitada());
                        validacoes.add(new ValidaStatusTrocaPedido());
                    }
                }
            }
            case null, default -> {
                System.err.println("Tipo de Entidade não suportado na hora de buscar as validacoes");
            }
        }
        return validacoes;
    }
}
