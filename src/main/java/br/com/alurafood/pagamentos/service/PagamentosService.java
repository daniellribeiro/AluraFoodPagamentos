package br.com.alurafood.pagamentos.service;

import br.com.alurafood.pagamentos.model.ItemDoPedido;
import br.com.alurafood.pagamentos.dto.PagamentoDTO;
import br.com.alurafood.pagamentos.http.PedidoClient;
import br.com.alurafood.pagamentos.model.Pagamento;
import br.com.alurafood.pagamentos.model.Status;
import br.com.alurafood.pagamentos.repository.PagamentoRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.constraints.NotNull;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class PagamentosService {
    @Autowired
    private PagamentoRepository repository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private PedidoClient pedido;

    public Page<PagamentoDTO> obterTodos(Pageable paginacao){
        return repository
                .findAll(paginacao)
                .map(p -> modelMapper
                        .map(p, PagamentoDTO.class));
    }

    public PagamentoDTO obterPorId(Long id){
        Pagamento pagamento = repository
                .findById(id)
                .orElseThrow(EntityNotFoundException::new);

        return modelMapper.map(pagamento, PagamentoDTO.class);
    }

    public PagamentoDTO criarPagamento(PagamentoDTO dto){
        Pagamento pagamento = modelMapper.map(dto,Pagamento.class);
        pagamento.setStatus(Status.CRIADO);
        repository.save(pagamento);

        return modelMapper.map(pagamento,PagamentoDTO.class);
    }

    public PagamentoDTO atualizarPagamento(Long id, PagamentoDTO dto){
        Pagamento pagamento = modelMapper.map(dto,Pagamento.class);
        pagamento.setId(id);
        pagamento.setStatus(Status.CRIADO);
        pagamento = repository.save(pagamento);

        return modelMapper.map(pagamento,PagamentoDTO.class);
    }

    public void excluirPagamento(Long id){
        repository.deleteById(id);
    }

    public void confirmarPagamento(Long id){
        Optional<Pagamento> pagamento = repository.findById(id);

        if(!pagamento.isPresent()){
            throw new EntityNotFoundException();
        }

        pagamento.get().setStatus(Status.CONFIRMADO);
        repository.save(pagamento.get());
        pedido.atualizaPagamento(pagamento.get().getPedidoId());
    }

    public void alteraStatus(Long id) {
        Optional<Pagamento> pagamento = repository.findById(id);

        if(!pagamento.isPresent()){
            throw new EntityNotFoundException();
        }

        pagamento.get().setStatus(Status.CONFIRMADO_SEM_INTEGRACAO);
        repository.save(pagamento.get());

    }

    public List<ItemDoPedido> obterItensPedidoPorId(@NotNull Long id) {
        Optional<Pagamento> pagamento = repository.findById(id);

        List<ItemDoPedido> itens = new ArrayList<ItemDoPedido>();

        if(pagamento.isPresent()) {
            itens = pedido.itensDoPedido(pagamento.get().getPedidoId());
        }
        return itens;
    }
}
