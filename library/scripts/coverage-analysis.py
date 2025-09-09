#!/usr/bin/env python3
"""Parse JaCoCo exec file to extract coverage metrics."""

import struct
import sys
from collections import defaultdict

def parse_jacoco_exec(filename):
    """Parse JaCoCo exec file and return coverage statistics."""
    
    with open(filename, 'rb') as f:
        # Read header
        header = f.read(5)
        if header != b'\x01\xc0\xc0\x01\x00':
            print(f"Invalid JaCoCo exec file header")
            return None
            
        # Skip version info  
        f.read(3)
        
        stats = {
            'total_classes': 0,
            'covered_classes': 0,
            'total_methods': 0,
            'covered_methods': 0,
            'total_lines': 0,
            'covered_lines': 0,
            'total_branches': 0,
            'covered_branches': 0
        }
        
        class_coverage = defaultdict(lambda: {'lines': 0, 'covered': 0})
        
        while True:
            # Read block type
            block_type_bytes = f.read(1)
            if not block_type_bytes:
                break
                
            block_type = ord(block_type_bytes)
            
            if block_type == 0x11:  # Session info block
                # Skip session info
                session_id_len = struct.unpack('>H', f.read(2))[0]
                f.read(session_id_len)  # session id
                f.read(8)  # start time
                f.read(8)  # dump time
                
            elif block_type == 0x01:  # Class execution data
                # Read class ID
                class_id = struct.unpack('>Q', f.read(8))[0]
                
                # Read class name length and name
                name_len = struct.unpack('>H', f.read(2))[0]
                class_name = f.read(name_len).decode('utf-8', errors='ignore')
                
                # Read probe data length
                probe_len = struct.unpack('>I', f.read(4))[0]
                probe_data = f.read(probe_len)
                
                # Count covered probes (simplified)
                covered = sum(1 for byte in probe_data if byte != 0)
                total = probe_len * 8  # Each byte has 8 bits/probes
                
                if total > 0:
                    stats['total_classes'] += 1
                    if covered > 0:
                        stats['covered_classes'] += 1
                        
                    # Rough line estimation (1 probe ≈ 1 line)
                    stats['total_lines'] += total
                    stats['covered_lines'] += covered
                    
                    class_coverage[class_name] = {
                        'lines': total,
                        'covered': covered
                    }
                    
            else:
                # Unknown block type, try to continue
                continue
                
        return stats, class_coverage

def main():
    exec_file = 'build/jacoco/test.exec'
    
    print(f"Analyzing JaCoCo coverage from: {exec_file}")
    print("=" * 60)
    
    result = parse_jacoco_exec(exec_file)
    if not result:
        print("Failed to parse exec file")
        return
        
    stats, class_coverage = result
    
    # Calculate percentages
    class_coverage_pct = (stats['covered_classes'] / stats['total_classes'] * 100) if stats['total_classes'] > 0 else 0
    line_coverage_pct = (stats['covered_lines'] / stats['total_lines'] * 100) if stats['total_lines'] > 0 else 0
    
    print("\n📊 OVERALL COVERAGE SUMMARY")
    print("-" * 40)
    print(f"Classes:  {stats['covered_classes']:,}/{stats['total_classes']:,} ({class_coverage_pct:.1f}%)")
    print(f"Lines:    {stats['covered_lines']:,}/{stats['total_lines']:,} ({line_coverage_pct:.1f}%)")
    
    print("\n📈 TOP 10 COVERED CLASSES")
    print("-" * 40)
    
    # Sort classes by coverage
    sorted_classes = sorted(
        class_coverage.items(), 
        key=lambda x: x[1]['covered'],
        reverse=True
    )[:10]
    
    for class_name, coverage in sorted_classes:
        short_name = class_name.split('/')[-1] if '/' in class_name else class_name
        pct = (coverage['covered'] / coverage['lines'] * 100) if coverage['lines'] > 0 else 0
        print(f"{short_name[:40]:<40} {pct:>5.1f}% ({coverage['covered']}/{coverage['lines']})")
    
    print("\n📉 CLASSES WITH NO COVERAGE")
    print("-" * 40)
    
    uncovered = [name for name, cov in class_coverage.items() if cov['covered'] == 0]
    print(f"Total uncovered classes: {len(uncovered)}")
    
    if uncovered[:5]:
        print("\nSample uncovered classes:")
        for class_name in uncovered[:5]:
            short_name = class_name.split('/')[-1] if '/' in class_name else class_name
            print(f"  - {short_name}")

if __name__ == '__main__':
    main()