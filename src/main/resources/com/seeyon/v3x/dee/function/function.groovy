package com.seeyon.v3x.dee.function

def uuid(){
	def o = new com.seeyon.v3x.dee.function.UUIDTag()
	o.execute()
}
def dict(dictName){
	def o = new com.seeyon.v3x.dee.function.DictionaryTag()
	o.setDictionary(dictName)
	o.setContext(func_context)
	o.setKey(func_value)
	o.execute()
}
def val(columnName){
	def row = func_currentRow
	def col = row.getChild(columnName)
	col == null ? null : col.value
}
def seq(seqName){
	def o = new com.seeyon.v3x.dee.function.SeqTag()
	o.setSeqName(seqName)
	o.setCurr(0)
	o.execute()
}
def curr(seqName){
	def o = new com.seeyon.v3x.dee.function.SeqTag()
	o.setSeqName(seqName)
	o.setCurr(1)
	o.execute()
}
