import torch
import torch.nn.functional as F
from torch_geometric.nn import SAGEConv
# We re-define the class to ensure exact architecture match for loading
class FraudGNN(torch.nn.Module):
    def __init__(self, in_channels, hidden_channels, out_channels):
        super().__init__()
        self.conv1 = SAGEConv(in_channels, hidden_channels)
        self.conv2 = SAGEConv(hidden_channels, hidden_channels)
        self.conv3 = SAGEConv(hidden_channels, out_channels)

    def forward(self, x, edge_index):
        x = self.conv1(x, edge_index)
        x = F.relu(x)
        x = F.dropout(x, p=0.3, training=self.training) # Note: Dropout is ignored in eval()
        x = self.conv2(x, edge_index)
        x = F.relu(x)
        x = F.dropout(x, p=0.3, training=self.training)
        x = self.conv3(x, edge_index)
        return F.log_softmax(x, dim=1)

# 1. Initialize Model
model = FraudGNN(in_channels=2, hidden_channels=64, out_channels=2)

# 2. Load the V2 Weights
print("🔄 Loading V2 Model Weights...")
try:
    model.load_state_dict(torch.load("fraud_gnn_model_neo4j_v2.pth"))
    print("✅ Weights loaded successfully.")
except FileNotFoundError:
    print("❌ Error: 'fraud_gnn_model_neo4j_v2.pth' not found. Did you run the v2 training script?")
    exit()

model.eval() # Set to evaluation mode (disables Dropout)

# 3. Create Dummy Input for Tracing
# The values don't matter, only the shape and data type.
# Batch size 1, 2 features (RiskScore, KYC)
dummy_x = torch.randn(1, 2)  
# Self-loop edge index for tracing
dummy_edge_index = torch.tensor([[0], [0]], dtype=torch.long) 

# 4. Export
print("🔄 Converting to ONNX...")
torch.onnx.export(
    model, 
    (dummy_x, dummy_edge_index), 
    "fraud_model_v2.onnx", # Output file
    export_params=True,
    opset_version=16, # Use a recent opset for PyG compatibility
    do_constant_folding=True,
    input_names = ['x', 'edge_index'],
    output_names = ['output'],
    dynamic_axes={
        'x': {0: 'num_nodes'},          
        'edge_index': {1: 'num_edges'}  
    }
)

print("✅ Success! Exported to 'fraud_model_v2.onnx'")