package com.victorhugo.boleiragem.ui.screens.compartilhar

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.victorhugo.boleiragem.data.model.GrupoPelada

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompartilharPeladaScreen(
    grupo: GrupoPelada,
    onCompartilhar: (String) -> Unit,
    onVoltar: () -> Unit
) {
    var mensagemAdicional by remember { mutableStateOf("") }
    val context = LocalContext.current
    
    // Obter o texto de compartilhamento baseado no grupo
    val textoCompartilhamento = remember(grupo, mensagemAdicional) {
        grupo.getTextoCompartilhamento(mensagemAdicional)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Compartilhar Pelada") },
                navigationIcon = {
                    IconButton(onClick = onVoltar) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack, 
                            contentDescription = "Voltar"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Compartilhar ${grupo.nome}",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Mensagem adicional personalizada
            OutlinedTextField(
                value = mensagemAdicional,
                onValueChange = { mensagemAdicional = it },
                label = { Text("Mensagem adicional (opcional)") },
                placeholder = { Text("Ex: Traga sua chuteira! Confirmem presença.") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Pré-visualização da mensagem
            Text(
                text = "Pré-visualização:",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = textoCompartilhamento,
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Botão para compartilhar
            Button(
                onClick = { onCompartilhar(textoCompartilhamento) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Compartilhar"
                )
                Text(
                    text = "Compartilhar",
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}
