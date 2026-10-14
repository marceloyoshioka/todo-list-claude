import { describe, it, expect } from 'vitest'
import { mensagemDeErro } from '../http'

describe('mensagemDeErro', () => {
  it('usa a mensagem enviada pela API', () => {
    const erro = { response: { status: 409, data: { mensagem: 'Categoria com tarefas.', erros: [] } } }
    expect(mensagemDeErro(erro)).toBe('Categoria com tarefas.')
  })

  it('anexa os erros de validacao (400)', () => {
    const erro = {
      response: {
        status: 400,
        data: { mensagem: 'Erro de validação nos dados enviados.', erros: ['O nome é obrigatório'] },
      },
    }
    expect(mensagemDeErro(erro)).toBe('Erro de validação nos dados enviados. O nome é obrigatório')
  })

  it('avisa quando a API esta fora do ar (sem resposta)', () => {
    expect(mensagemDeErro({ message: 'Network Error' })).toContain('Não foi possível conectar')
  })

  it('cai no status HTTP quando o corpo nao tem mensagem', () => {
    expect(mensagemDeErro({ response: { status: 502, data: '' } })).toBe('Erro 502 ao falar com a API.')
  })
})
